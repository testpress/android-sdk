package `in`.testpress.course.fragments

import `in`.testpress.course.util.WebViewFactory
import `in`.testpress.course.R
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.util.webview.WebViewEventListener
import `in`.testpress.util.webview.BaseWebViewClient
import `in`.testpress.util.webview.BaseWebChromeClient
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
        const val ARG_LEARNLENS_ASSET_ID = "learnlensAssetId"
        const val DEFAULT_TEMPLATE = "learnlens_template.html"
    }
    
    private var webView: WebView? = null
    private var container: FrameLayout? = null
    private var progressBar: ProgressBar? = null
    private var emptyViewContainer: FrameLayout? = null
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var webChromeClient: BaseWebChromeClient
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ai_pdf_fragment, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val args = extractArguments()
        initializeViews(view)
        initializeEmptyViewFragment()
        webChromeClient = BaseWebChromeClient(this)
        loadPdfInWebView(args)
    }
    
    private fun extractArguments(): PdfArguments {
        val contentId = requireArguments().getLong(ARG_CONTENT_ID, -1L)
        val courseId = requireArguments().getLong(ARG_COURSE_ID, -1L)
        val pdfUrl = requireArguments().getString(ARG_PDF_URL)
        val pdfTitle = requireArguments().getString(ARG_PDF_TITLE) ?: "PDF Document"
        val templateName = requireArguments().getString(ARG_TEMPLATE_NAME) ?: DEFAULT_TEMPLATE
        val learnlensAssetId = requireArguments().getString(ARG_LEARNLENS_ASSET_ID)
        
        require(contentId != -1L && courseId != -1L && !pdfUrl.isNullOrEmpty()) {
            "Required arguments are missing or invalid"
        }
        
        return PdfArguments(contentId, courseId, pdfUrl, pdfTitle, templateName, learnlensAssetId)
    }
    
    private fun initializeViews(view: View) {
        container = view.findViewById(R.id.aiPdf_view_fragment)
        progressBar = view.findViewById(R.id.pb_loading)
        emptyViewContainer = view.findViewById(R.id.empty_view_container)
    }
    
    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }
    
    private fun loadPdfInWebView(args: PdfArguments) {
        val cacheKey = "pdf_template_${args.contentId}"
        val isNewWebView = !WebViewFactory.isCached(args.contentId, cacheKey)
        
        if (isNewWebView) showLoading()
        
        webView = WebViewFactory.createCached(
            contentId = args.contentId,
            cacheKey = cacheKey,
            loadUrl = false,
            createWebView = { WebView(requireContext()) }
        ) { wv ->
            configureWebView(wv, args)
        }
        
        if (!isNewWebView) hideLoading()
        
        container?.let { cont -> 
            webView?.let { wv -> 
                WebViewFactory.attach(cont, wv)
                wv.requestFocus()
            }
        }
    }
    
    private fun configureWebView(wv: WebView, args: PdfArguments) {
        wv.enableFileAccess()
        wv.webViewClient = BaseWebViewClient(this)
        wv.webChromeClient = webChromeClient
        
        val authToken = TestpressSdk.getTestpressSession(requireContext())?.token ?: ""
        val cacheDir = File(requireContext().filesDir, "web_assets")
        val pdfId = args.learnlensAssetId ?: args.contentId.toString()
        
        wv.loadTemplateAndCacheResources(
            templateName = args.templateName,
            replacements = mapOf(
                "PDF_URL" to args.pdfUrl,
                "PDF_ID" to pdfId,
                "AUTH_TOKEN" to authToken,
                "PDF_TITLE" to args.pdfTitle
            ),
            baseUrl = "file://${cacheDir.absolutePath}/"
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        webChromeClient.cleanup()
        WebViewFactory.detach(webView)
        webView = null
        container = null
        progressBar = null
        emptyViewContainer = null
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
    
    override fun onLoadingStarted() = showLoading()
    override fun onLoadingFinished() = hideLoading()
    override fun onError(exception: TestpressException) = showErrorView(exception)
    override fun isViewActive(): Boolean = isAdded
    
    private data class PdfArguments(
        val contentId: Long,
        val courseId: Long,
        val pdfUrl: String,
        val pdfTitle: String,
        val templateName: String,
        val learnlensAssetId: String?
    )
}
