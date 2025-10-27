package `in`.testpress.course.fragments

import `in`.testpress.course.R
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
        
        if (contentId == -1L || courseId == -1L) {
            throw IllegalArgumentException("Required arguments (contentId, courseId) are missing or invalid.")
        }
        
        container = view.findViewById(R.id.aiPdf_view_fragment)
        progressBar = view.findViewById(R.id.pb_loading)
        emptyViewContainer = view.findViewById(R.id.empty_view_container)
        
        initializeEmptyViewFragment()
        
        val pdfUrl = getPdfUrl(courseId, contentId)
        
        val isNewWebView = !PdfWebViewCache.isCached(contentId, pdfUrl)
        
        if (isNewWebView) {
            showLoading()
        }
        
        webView = PdfWebViewCache.acquire(contentId, pdfUrl) { wv ->
            wv.webViewClient = AIChatWebViewClient()
            wv.webChromeClient = AIChatWebChromeClient()
        }
        
        if (!isNewWebView) {
            hideLoading()
        }
        
        container?.let { cont -> 
            webView?.let { wv -> PdfWebViewCache.attach(cont, wv) }
        }
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

    private fun getPdfUrl(courseId: Long, contentId: Long): String {
        val session = TestpressSdk.getTestpressSession(requireContext()) 
            ?: throw IllegalStateException("User session not found.")
        val baseUrl = session.instituteSettings?.domainUrl.takeIf { !it.isNullOrEmpty() }
            ?: throw IllegalStateException("Base URL not configured.")
        return "$baseUrl/courses/$courseId/contents/$contentId/?content_detail_v2=true"
    }

    private inner class AIChatWebViewClient : WebViewClient() {
        
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            if (isAdded) {
                errorList.clear()  // Clear errors from previous page
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
