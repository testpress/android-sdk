package `in`.testpress.course.fragments

import `in`.testpress.course.util.CacheWebView
import `in`.testpress.course.R
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.util.webview.WebViewEventListener
import `in`.testpress.util.webview.BaseWebViewClient
import `in`.testpress.util.webview.WebView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import java.io.File

class AIChatPdfFragment : Fragment(), EmptyViewListener, WebViewEventListener {
    
    companion object {
        const val ARG_CONTENT_ID = "contentId"
        const val ARG_COURSE_ID = "courseId"
        const val ARG_PDF_URL = "pdfUrl"
        const val ARG_PDF_TITLE = "pdfTitle"
        const val ARG_TEMPLATE_NAME = "templateName"
        const val DEFAULT_TEMPLATE = "learnlens_template.html"
    }
    
    private var webView: WebView? = null
    private var container: FrameLayout? = null
    private var progressBar: ProgressBar? = null
    private var emptyViewContainer: FrameLayout? = null
    private lateinit var emptyViewFragment: EmptyViewFragment
    
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
        val templateName = requireArguments().getString(ARG_TEMPLATE_NAME) ?: DEFAULT_TEMPLATE
        
        require(contentId != -1L && courseId != -1L && !pdfUrl.isNullOrEmpty()) {
            "Required arguments are missing or invalid"
        }
        
        container = view.findViewById(R.id.aiPdf_view_fragment)
        progressBar = view.findViewById(R.id.pb_loading)
        emptyViewContainer = view.findViewById(R.id.empty_view_container)
        
        initializeEmptyViewFragment()
        
        val cacheKey = "pdf_template_$contentId"
        val isNewWebView = !CacheWebView.isCached(contentId, cacheKey)
        
        if (isNewWebView) {
            showLoading()
        }
        
        webView = CacheWebView.acquire(
            contentId = contentId,
            url = cacheKey,
            loadUrl = false,
            createWebView = { WebView(requireContext()) }
        ) { wv ->
            configureWebView(wv, pdfUrl, pdfTitle, contentId.toString(), templateName)
        }
        
        if (!isNewWebView) {
            hideLoading()
        }
        
        container?.let { cont -> 
            webView?.let { wv -> CacheWebView.attach(cont, wv) }
        }
    }
    
    private fun configureWebView(wv: WebView, pdfUrl: String, pdfTitle: String, pdfId: String, templateName: String) {
        wv.enableFileAccess()
        wv.webViewClient = BaseWebViewClient(this)
        
        val authToken = TestpressSdk.getTestpressSession(requireContext())?.token ?: ""
        val cacheDir = File(requireContext().filesDir, "web_assets")
        
        wv.loadTemplateAndCacheResources(
            templateName = templateName,
            replacements = mapOf(
                "PDF_URL" to pdfUrl,
                "PDF_ID" to pdfId,
                "AUTH_TOKEN" to authToken,
                "PDF_TITLE" to pdfTitle
            ),
            baseUrl = "file://${cacheDir.absolutePath}/"
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        CacheWebView.detach(webView)
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
    
    override fun onLoadingStarted() {
        showLoading()
    }
    
    override fun onLoadingFinished() {
        hideLoading()
    }
    
    override fun onError(exception: TestpressException) {
        showErrorView(exception)
    }
    
    override fun isViewActive(): Boolean {
        return isAdded
    }
    
}
