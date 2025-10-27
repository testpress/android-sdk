package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.util.PdfWebViewCache
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.util.webview.EmptyViewFragment
import `in`.testpress.util.webview.EmptyViewListener
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
 * Fragment that displays PDF using a cached WebView.
 * Includes full UI states from WebViewFragment: loading, error, content.
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
    
    private val errorList = linkedMapOf<WebResourceRequest?, WebResourceResponse?>()
    
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
        
        // Initialize UI elements
        container = view.findViewById(R.id.aiPdf_view_fragment)
        progressBar = view.findViewById(R.id.pb_loading)
        emptyViewContainer = view.findViewById(R.id.empty_view_container)
        
        // Setup error view (same as WebViewFragment)
        initializeEmptyViewFragment()
        
        // Show loading
        showLoading()
        
        // Get PDF URL
        val pdfUrl = getPdfUrl(courseId, contentId)
        
        // Get cached WebView (or create new one)
        webView = PdfWebViewCache.acquire(contentId, pdfUrl).apply {
            // Set WebViewClient with error handling (like WebViewFragment)
            webViewClient = AIChatWebViewClient()
            // Set WebChromeClient with progress (like WebViewFragment)
            webChromeClient = AIChatWebChromeClient()
        }
        
        // Attach to container
        container?.let { PdfWebViewCache.attach(it, webView!!) }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // Detach but don't destroy - stays in cache!
        PdfWebViewCache.detach(webView)
        
        webView = null
        container = null
        progressBar = null
        emptyViewContainer = null
    }
    
    /**
     * Initialize error view fragment (same as WebViewFragment).
     */
    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }
    
    /**
     * Show loading state (same as WebViewFragment).
     */
    private fun showLoading() {
        progressBar?.visibility = View.VISIBLE
        container?.visibility = View.GONE
        emptyViewContainer?.visibility = View.GONE
    }
    
    /**
     * Hide loading state (same as WebViewFragment).
     */
    private fun hideLoading() {
        progressBar?.visibility = View.GONE
        container?.visibility = View.VISIBLE
        emptyViewContainer?.visibility = View.GONE
    }
    
    /**
     * Show error view (same as WebViewFragment).
     */
    private fun showErrorView(exception: TestpressException) {
        progressBar?.visibility = View.GONE
        container?.visibility = View.GONE
        emptyViewContainer?.isVisible = true
        emptyViewFragment.displayError(exception)
    }
    
    /**
     * Retry loading on error (same as WebViewFragment).
     */
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
    
    /**
     * WebViewClient that mimics CustomWebViewClient behavior.
     * Handles errors, page states, exactly like WebViewFragment.
     */
    private inner class AIChatWebViewClient : WebViewClient() {
        
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            // Show loading when page starts (like WebViewFragment)
            showLoading()
        }
        
        override fun onPageFinished(view: WebView?, url: String?) {
            // Hide loading when page finishes (like WebViewFragment)
            hideLoading()
            checkWebViewHasError()
        }
        
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            // Handle errors same as CustomWebViewClient
            val requestUrl = request?.url.toString()
            val currentWebViewUrl = webView?.url.toString()
            if (requestUrl == currentWebViewUrl) {
                val exception = TestpressException.unexpectedWebViewError(
                    Exception("WebView error ${error?.errorCode}")
                )
                showErrorView(exception)
            }
        }
        
        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            // Track HTTP errors same as CustomWebViewClient
            errorList[request] = errorResponse
        }
        
        /**
         * Check if WebView has HTTP errors (same logic as CustomWebViewClient).
         */
        private fun checkWebViewHasError() {
            errorList.forEach { error ->
                val requestUrl = error.key?.url.toString()
                val currentWebViewUrl = webView?.url.toString()
                if (requestUrl == currentWebViewUrl) {
                    val statusCode = error.value?.statusCode ?: -1
                    val reasonPhrase = error.value?.reasonPhrase ?: "Unknown Error"
                    val httpError = TestpressException.httpError(statusCode, reasonPhrase)
                    showErrorView(httpError)
                    errorList.clear()
                }
            }
        }
    }
    
    /**
     * WebChromeClient that handles progress (like CustomWebChromeClient).
     */
    private inner class AIChatWebChromeClient : WebChromeClient() {
        
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            
            Log.d(TAG, "Loading progress: $newProgress%")
            
            // Hide loading when fully loaded
            if (newProgress == 100) {
                hideLoading()
            }
        }
        
        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
            // Log console messages (like WebViewFragment)
            consoleMessage?.let {
                Log.d(TAG, "Console: ${it.message()} (${it.sourceId()}:${it.lineNumber()})")
            }
            return true
        }
    }
}
