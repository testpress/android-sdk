package `in`.testpress.course.fragments

import `in`.testpress.course.util.WebViewCache
import `in`.testpress.course.util.LocalWebFileCache
import `in`.testpress.course.R
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.io.File

class AIChatPdfFragment : Fragment(), EmptyViewListener {
    
    companion object {
        private const val ARG_CONTENT_ID = "contentId"
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_PDF_URL = "pdfUrl"
        private const val ARG_PDF_TITLE = "pdfTitle"
        
        private const val LEARNLENS_JS_URL = "https://static.testpress.in/static-staging/learnlens/learnlens-pdfchat.iife.js"
        private const val LEARNLENS_CSS_URL = "https://static.testpress.in/static-staging/learnlens/learnlens-frontend.css"
        private const val LEARNLENS_JS_FILE = "learnlens.js"
        private const val LEARNLENS_CSS_FILE = "learnlens.css"
    }
    
    private var webView: WebView? = null
    private var container: FrameLayout? = null
    private var progressBar: ProgressBar? = null
    private var emptyViewContainer: FrameLayout? = null
    private lateinit var emptyViewFragment: EmptyViewFragment
    private val errorList = linkedMapOf<String, WebResourceResponse?>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ai_pdf_fragment, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val contentId = requireArguments().getLong(ARG_CONTENT_ID, -1L)
        val courseId = requireArguments().getLong(ARG_COURSE_ID, -1L)
        val pdfUrl = requireArguments().getString(ARG_PDF_URL)
        val pdfTitle = requireArguments().getString(ARG_PDF_TITLE) ?: "PDF Document"
        
        require(contentId != -1L && courseId != -1L && !pdfUrl.isNullOrEmpty()) {
            "Required arguments are missing or invalid"
        }
        
        container = view.findViewById(R.id.aiPdf_view_fragment)
        progressBar = view.findViewById(R.id.pb_loading)
        emptyViewContainer = view.findViewById(R.id.empty_view_container)
        
        initializeEmptyViewFragment()
        downloadLearnLensAssets()
        
        val cacheKey = "learnlens_$contentId"
        val isNewWebView = !WebViewCache.isCached(contentId, cacheKey)
        
        if (isNewWebView) {
            showLoading()
        }
        
        webView = WebViewCache.acquire(contentId, cacheKey, loadUrl = false) { wv ->
            configureWebView(wv, pdfUrl, pdfTitle, contentId.toString())
        }
        
        if (!isNewWebView) {
            hideLoading()
        }
        
        container?.let { cont -> 
            webView?.let { wv -> WebViewCache.attach(cont, wv) }
        }
    }
    
    private fun configureWebView(wv: WebView, pdfUrl: String, pdfTitle: String, pdfId: String) {
        enableFileAccess(wv)
        wv.webViewClient = AIChatWebViewClient()
        loadLearnLensHtml(wv, pdfUrl, pdfTitle, pdfId)
    }
    
    private fun enableFileAccess(wv: WebView) {
        wv.settings.allowFileAccess = true
        wv.settings.allowFileAccessFromFileURLs = true
        wv.settings.allowUniversalAccessFromFileURLs = true
        WebView.setWebContentsDebuggingEnabled(true)
    }
    
    private fun downloadLearnLensAssets() {
        val assets = listOf(
            LEARNLENS_JS_URL to LEARNLENS_JS_FILE,
            LEARNLENS_CSS_URL to LEARNLENS_CSS_FILE
        )
        LocalWebFileCache.downloadMultipleInBackground(requireContext(), assets)
    }
    
    private fun loadLearnLensHtml(wv: WebView, pdfUrl: String, pdfTitle: String, pdfId: String) {
        val authToken = TestpressSdk.getTestpressSession(requireContext())?.token ?: ""
        
        val jsPath = LocalWebFileCache.getLocalPath(requireContext(), LEARNLENS_JS_FILE, LEARNLENS_JS_URL)
        val cssPath = LocalWebFileCache.getLocalPath(requireContext(), LEARNLENS_CSS_FILE, LEARNLENS_CSS_URL)
        
        val html = LocalWebFileCache.loadTemplate(requireContext(), "learnlens.html", mapOf(
            "JS_URL" to jsPath,
            "CSS_URL" to cssPath,
            "PDF_URL" to pdfUrl,
            "PDF_ID" to pdfId,
            "AUTH_TOKEN" to authToken,
            "PDF_TITLE" to pdfTitle
        ))
        
        val cacheDir = File(requireContext().filesDir, "web_assets")
        wv.loadDataWithBaseURL("file://${cacheDir.absolutePath}/", html, "text/html", "UTF-8", null)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        WebViewCache.detach(webView)
        errorList.clear()
        webView = null
        container = null
        progressBar = null
        emptyViewContainer = null
    }
    
    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }
    
    private fun showLoading() {
        progressBar?.visibility = View.VISIBLE
        container?.visibility = View.GONE
        emptyViewContainer?.visibility = View.GONE
    }
    
    private fun hideLoading() {
        progressBar?.visibility = View.GONE
        container?.visibility = View.VISIBLE
        emptyViewContainer?.visibility = View.GONE
    }
    
    private fun showErrorView(exception: TestpressException) {
        progressBar?.visibility = View.GONE
        container?.visibility = View.GONE
        emptyViewContainer?.isVisible = true
        emptyViewFragment.displayError(exception)
    }
    
    override fun onRetryClick() {
        emptyViewContainer?.isVisible = false
        container?.isVisible = true
        showLoading()
        webView?.reload()
    }
    
    private inner class AIChatWebViewClient : WebViewClient() {
        
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if (isAdded) {
                errorList.clear()
                showLoading()
            }
        }
        
        override fun onPageFinished(view: WebView?, url: String?) {
            if (isAdded) {
                hideLoading()
                if (errorList.isNotEmpty()) {
                    checkWebViewHasError(view)
                }
            }
        }
        
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            if (!isAdded) return
            
            if (request?.isForMainFrame == true) {
                showErrorView(TestpressException.unexpectedWebViewError(
                    Exception("WebView error ${error?.errorCode}")
                ))
            }
        }
        
        override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
            request?.url?.toString()?.let { url ->
                errorList[url] = errorResponse
            }
        }
        
        private fun checkWebViewHasError(view: WebView?) {
            if (!isAdded) return
            
            val currentUrl = view?.url ?: return
            errorList[currentUrl]?.let { response ->
                val statusCode = response.statusCode
                val reasonPhrase = response.reasonPhrase ?: "Unknown Error"
                showErrorView(TestpressException.httpError(statusCode, reasonPhrase))
                errorList.remove(currentUrl)
            }
        }
    }
    
}
