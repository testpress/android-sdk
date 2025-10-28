package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.util.LearnLensAssetManager
import `in`.testpress.course.util.PdfWebViewCache
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.io.File

/**
 * Displays PDF content in AI chat view using cached WebView instances.
 * 
 * Leverages PdfWebViewCache for instant switching between PDFs without reloading.
 * Includes loading states, error handling, and retry functionality.
 */
class AIChatPdfFragment : Fragment(), EmptyViewListener {
    
    companion object {
        private const val TAG = "AIChatPdfFragment"
        private const val ARG_CONTENT_ID = "contentId"
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_PDF_URL = "pdfUrl"
        private const val ARG_PDF_TITLE = "pdfTitle"
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
        LearnLensAssetManager.downloadAssetsInBackground(requireContext())
        
        val cacheKey = "learnlens_$contentId"
        val isNewWebView = !PdfWebViewCache.isCached(contentId, cacheKey)
        
        if (isNewWebView) {
            showLoading()
        }
        
        webView = PdfWebViewCache.acquire(contentId, cacheKey, loadUrl = false) { wv ->
            configureWebView(wv, pdfUrl, pdfTitle, contentId.toString())
        }
        
        if (!isNewWebView) {
            hideLoading()
        }
        
        container?.let { cont -> 
            webView?.let { wv -> PdfWebViewCache.attach(cont, wv) }
        }
    }
    
    private fun configureWebView(wv: WebView, pdfUrl: String, pdfTitle: String, pdfId: String) {
        enableFileAccess(wv)
        wv.webViewClient = AIChatWebViewClient()
        wv.webChromeClient = AIChatWebChromeClient()
        loadLearnLensHtml(wv, pdfUrl, pdfTitle, pdfId)
    }
    
    private fun enableFileAccess(wv: WebView) {
        wv.settings.allowFileAccess = true
        wv.settings.allowFileAccessFromFileURLs = true
        wv.settings.allowUniversalAccessFromFileURLs = true
        WebView.setWebContentsDebuggingEnabled(true)
    }
    
    private fun loadLearnLensHtml(wv: WebView, pdfUrl: String, pdfTitle: String, pdfId: String) {
        val authToken = TestpressSdk.getTestpressSession(requireContext())?.token ?: ""
        val html = LearnLensAssetManager.generateLearnLensHtml(
            requireContext(), pdfUrl, pdfTitle, pdfId, authToken
        )
        val cacheDir = File(requireContext().filesDir, "learnlens_cache")
        wv.loadDataWithBaseURL("file://${cacheDir.absolutePath}/", html, "text/html", "UTF-8", null)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        PdfWebViewCache.detach(webView)
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
                checkWebViewHasError()
            }
        }
        
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            if (!isAdded) return
            
            val requestUrl = request?.url.toString()
            val currentWebViewUrl = webView?.url.toString()
            if (requestUrl == currentWebViewUrl) {
                showErrorView(TestpressException.unexpectedWebViewError(
                    Exception("WebView error ${error?.errorCode}")
                ))
            }
        }
        
        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            request?.url?.toString()?.let { url ->
                errorList[url] = errorResponse
            }
        }
        
        private fun checkWebViewHasError() {
            if (!isAdded) return
            
            val currentWebViewUrl = webView?.url?.toString() ?: return
            
            errorList[currentWebViewUrl]?.let { response ->
                val statusCode = response.statusCode
                val reasonPhrase = response.reasonPhrase ?: "Unknown Error"
                showErrorView(TestpressException.httpError(statusCode, reasonPhrase))
                errorList.clear()
            }
        }
    }
    

    private inner class AIChatWebChromeClient : WebChromeClient() {
        
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (isAdded && newProgress == 100) {
                hideLoading()
            }
        }
        
        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
            consoleMessage?.let {
                Log.d(TAG, "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}")
            }
            return true
        }
    }
}
