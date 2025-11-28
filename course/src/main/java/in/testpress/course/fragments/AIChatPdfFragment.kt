package `in`.testpress.course.fragments

import `in`.testpress.course.util.WebViewFactory
import `in`.testpress.course.util.LearnLensHelper
import `in`.testpress.course.R
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressCallback
import `in`.testpress.course.network.NetworkBookmark
import `in`.testpress.course.network.BookmarksListApiResponse
import `in`.testpress.course.repository.BookmarkRepository
import `in`.testpress.course.util.LearnLensBridge
import `in`.testpress.v2_4.models.ApiResponse
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.util.webview.WebViewEventListener
import `in`.testpress.util.webview.BaseWebViewClient
import `in`.testpress.util.webview.BaseWebChromeClient
import `in`.testpress.util.webview.WebView
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import `in`.testpress.core.TestpressException
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
    private var isWebViewCached = false
    private var isLearnLensReady = false
    private var isCheckingLearnLens = false
    private var learnLensHelper: LearnLensHelper? = null
    private var pendingBookmarks: List<NetworkBookmark>? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ai_pdf_fragment, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        pendingBookmarks = null
        learnLensHelper = null
        isLearnLensReady = false
        isCheckingLearnLens = false
        
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
        val wasCachedBefore = WebViewFactory.isCached(args.contentId, cacheKey)
        
        if (wasCachedBefore) {
            val repository = BookmarkRepository(requireContext())
            val cachedBookmarks = repository.getCachedBookmarks(args.contentId, "annotate")
            
            var isNewWebView = false
            
            webView = WebViewFactory.createCached(
                contentId = args.contentId,
                cacheKey = cacheKey,
                loadUrl = false,
                createWebView = { WebView(requireContext()) }
            ) { wv ->
                isNewWebView = true
                configureWebView(wv, args, cachedBookmarks)
            }
            
            isWebViewCached = !isNewWebView
            
            container?.let { cont -> 
                webView?.let { wv -> 
                    WebViewFactory.attach(cont, wv)
                    wv.requestFocus()
                }
            }
            
            hideLoading()
            checkLearnLensReadiness { isReady ->
                if (isReady) {
                    isLearnLensReady = true
                }
                fetchBookmarks(args.contentId) { bookmarks ->
                    pendingBookmarks = bookmarks
                    val bookmarksChanged = bookmarks.size != cachedBookmarks.size || 
                        bookmarks.map { it.id }.toSet() != cachedBookmarks.map { it.id }.toSet()
                    if (bookmarksChanged && bookmarks.isNotEmpty()) {
                        injectAnnotations(bookmarks)
                    }
                }
            }
        } else {
            showLoading()
            
            val repository = BookmarkRepository(requireContext())
            val cachedBookmarks = repository.getCachedBookmarks(args.contentId, "annotate")
            
            var isNewWebView = false
            webView = WebViewFactory.createCached(
                contentId = args.contentId,
                cacheKey = cacheKey,
                loadUrl = false,
                createWebView = { WebView(requireContext()) }
            ) { wv ->
                isNewWebView = true
                configureWebView(wv, args, cachedBookmarks)
            }
            
            isWebViewCached = !isNewWebView
            
            container?.let { cont -> 
                webView?.let { wv -> 
                    WebViewFactory.attach(cont, wv)
                    wv.requestFocus()
                }
            }
            
            fetchBookmarks(args.contentId) { bookmarks ->
                pendingBookmarks = bookmarks
                if (bookmarks.isNotEmpty()) {
                    val bookmarksChanged = bookmarks.size != cachedBookmarks.size || 
                        bookmarks.map { it.id }.toSet() != cachedBookmarks.map { it.id }.toSet()
                    if (bookmarksChanged || cachedBookmarks.isEmpty()) {
                        webView?.let { wv ->
                            initializeHelper(wv)
                            learnLensHelper?.injectBookmarks(bookmarks)
                        }
                    }
                }
            }
        }
    }
    
    private fun injectAnnotations(bookmarks: List<NetworkBookmark>) {
        webView?.let { wv ->
            initializeHelper(wv)
            learnLensHelper?.injectBookmarks(bookmarks)
        }
    }
    
    private fun checkLearnLensReadiness(onComplete: (Boolean) -> Unit) {
        if (isCheckingLearnLens) return
        
        webView?.let { wv ->
            initializeHelper(wv)
            isCheckingLearnLens = true
            
            learnLensHelper?.checkReadiness(
                onReady = {
                    isCheckingLearnLens = false
                    onComplete(true)
                },
                onNotReady = {
                    isCheckingLearnLens = false
                    onComplete(false)
                }
            )
        }
    }
    
    private fun fetchBookmarks(contentId: Long, onComplete: (List<NetworkBookmark>) -> Unit) {
        val repository = BookmarkRepository(requireContext())
        
        val cachedBookmarks = repository.getCachedBookmarks(contentId, "annotate")
        if (cachedBookmarks.isNotEmpty()) {
            onComplete(cachedBookmarks)
        }
        
        val queryParams = hashMapOf<String, Any>(
            "content_type" to "chapter_content",
            "object_id" to contentId,
            "bookmark_type" to "annotate"
        )
        
        repository.getBookmarks(queryParams, object : TestpressCallback<ApiResponse<BookmarksListApiResponse>>() {
            override fun onSuccess(response: ApiResponse<BookmarksListApiResponse>) {
                val bookmarks = response.results?.bookmarks ?: emptyList()
                if (cachedBookmarks.isEmpty() || bookmarks.isNotEmpty()) {
                    onComplete(bookmarks)
                }
            }
            
            override fun onException(exception: TestpressException?) {
                if (cachedBookmarks.isEmpty()) {
                    onComplete(emptyList())
                }
            }
        })
    }
    
    private fun initializeHelper(wv: WebView) {
        if (learnLensHelper == null) {
            learnLensHelper = LearnLensHelper(requireContext(), wv)
        }
    }

    
    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun configureWebView(
        wv: WebView,
        args: PdfArguments,
        bookmarks: List<NetworkBookmark>
    ) {
        wv.enableFileAccess()
        wv.webViewClient = BaseWebViewClient(this)
        wv.webChromeClient = webChromeClient
        
        val authToken = TestpressSdk.getTestpressSession(requireContext())?.token ?: ""
        val cacheDir = File(requireContext().filesDir, "web_assets")
        val pdfId = args.learnlensAssetId ?: args.contentId.toString()
        
        initializeHelper(wv)
        val replacements = learnLensHelper?.buildTemplateReplacements(
            args.pdfUrl, pdfId, authToken, args.pdfTitle, bookmarks
        ) ?: emptyMap()
        
        wv.addJavascriptInterface(
            LearnLensBridge(requireActivity(), wv, args.contentId),
            "AndroidBridge"
        )
        
        wv.loadTemplateAndCacheResources(
            templateName = args.templateName,
            replacements = replacements,
            baseUrl = "file://${cacheDir.absolutePath}/"
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        isCheckingLearnLens = false
        isLearnLensReady = false
        pendingBookmarks = null
        webChromeClient.cleanup()
        WebViewFactory.detach(webView)
        webView = null
        container = null
        progressBar = null
        emptyViewContainer = null
        learnLensHelper = null
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
        if (!isWebViewCached) {
            showLoading()
            pendingBookmarks?.let { bookmarks ->
                webView?.let { wv ->
                    initializeHelper(wv)
                    learnLensHelper?.injectBookmarks(bookmarks)
                }
            }
        }
    }
    
    override fun onLoadingFinished() {
        if (isWebViewCached) return
        if (isCheckingLearnLens) return
        
        webView?.let { wv ->
            initializeHelper(wv)
            checkLearnLensReadiness { isReady ->
                if (isReady) {
                    isLearnLensReady = true
                }
                hideLoading()
                injectPendingAnnotations()
                if (pendingBookmarks == null) {
                    webView?.postDelayed({
                        injectPendingAnnotations()
                    }, 500)
                }
            }
        } ?: run {
            hideLoading()
            injectPendingAnnotations()
            if (pendingBookmarks == null) {
                webView?.postDelayed({
                    injectPendingAnnotations()
                }, 500)
            }
        }
    }
    
    private fun injectPendingAnnotations() {
        pendingBookmarks?.let { bookmarks ->
            if (bookmarks.isNotEmpty()) {
                injectAnnotations(bookmarks)
            }
        }
    }
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
