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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import `in`.testpress.core.TestpressException
import java.io.File
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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
        resetState()
        val args = extractArguments()
        initializeViews(view)
        webChromeClient = BaseWebChromeClient(this)
        loadPdfInWebView(args)
    }
    
    private fun resetState() {
        pendingBookmarks = null
        learnLensHelper = null
        isLearnLensReady = false
        isCheckingLearnLens = false
    }
    
    private fun extractArguments(): PdfArguments {
        val args = requireArguments()
        val contentId = args.getLong(ARG_CONTENT_ID, -1L)
        val courseId = args.getLong(ARG_COURSE_ID, -1L)
        val pdfUrl = args.getString(ARG_PDF_URL)
        require(contentId != -1L && courseId != -1L && !pdfUrl.isNullOrEmpty()) {
            "Required arguments are missing or invalid"
        }
        return PdfArguments(
            contentId, courseId, pdfUrl,
            args.getString(ARG_PDF_TITLE) ?: "PDF Document",
            args.getString(ARG_TEMPLATE_NAME) ?: DEFAULT_TEMPLATE,
            args.getString(ARG_LEARNLENS_ASSET_ID)
        )
    }
    
    private fun initializeViews(view: View) {
        container = view.findViewById(R.id.aiPdf_view_fragment)
        progressBar = view.findViewById(R.id.pb_loading)
        emptyViewContainer = view.findViewById(R.id.empty_view_container)
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction().replace(R.id.empty_view_container, emptyViewFragment).commit()
    }
    
    private fun loadPdfInWebView(args: PdfArguments) {
        val cacheKey = "pdf_template_${args.contentId}"
        val wasCachedBefore = WebViewFactory.isCached(args.contentId, cacheKey)
        val repository = BookmarkRepository(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            val cachedBookmarks = repository.getCachedBookmarks(args.contentId, "annotate")
            showLoading()
            
            if (cachedBookmarks.isEmpty() && !wasCachedBefore) {
                fetchBookmarksForFirstLoad(args, cacheKey, wasCachedBefore)
            } else {
                setupWebView(args, cacheKey, cachedBookmarks, wasCachedBefore)
            }
        }
    }
    
    private fun fetchBookmarksForFirstLoad(args: PdfArguments, cacheKey: String, wasCachedBefore: Boolean) {
        val repository = BookmarkRepository(requireContext())
        val queryParams = hashMapOf<String, Any>(
            "content_type" to "chapter_content", "object_id" to args.contentId, "bookmark_type" to "annotate"
        )
        repository.getBookmarks(queryParams, object : TestpressCallback<ApiResponse<BookmarksListApiResponse>>() {
            override fun onSuccess(response: ApiResponse<BookmarksListApiResponse>) {
                pendingBookmarks = response.results?.bookmarks ?: emptyList()
                setupWebView(args, cacheKey, pendingBookmarks ?: emptyList(), wasCachedBefore)
            }
            override fun onException(exception: TestpressException?) {
                setupWebView(args, cacheKey, emptyList(), wasCachedBefore)
            }
        })
    }
    
    private fun checkLearnLensReadiness(onComplete: (Boolean) -> Unit) {
        if (isCheckingLearnLens) return
        
        webView?.let { wv ->
            initializeHelper(wv)
            isCheckingLearnLens = true
            learnLensHelper?.checkReadiness(
                onReady = { isCheckingLearnLens = false; onComplete(true) },
                onNotReady = { isCheckingLearnLens = false; onComplete(false) }
            )
        }
    }
    
    private fun setupWebView(args: PdfArguments, cacheKey: String, cachedBookmarks: List<NetworkBookmark>, wasCachedBefore: Boolean) {
        var isNewWebView = false
        webView = WebViewFactory.createCached(
            contentId = args.contentId,
            cacheKey = cacheKey,
            loadUrl = false,
            createWebView = { WebView(requireContext()) }
        ) { wv -> isNewWebView = true; configureWebView(wv, args, cachedBookmarks) }

        isWebViewCached = !isNewWebView
        container?.let { cont -> webView?.let { wv -> WebViewFactory.attach(cont, wv); wv.requestFocus() } }
        if (wasCachedBefore) loadBookmarksForCachedWebView(args, cachedBookmarks)
        else loadBookmarksForNewWebView(args, cachedBookmarks)
    }

    private fun loadBookmarksForCachedWebView(args: PdfArguments, cachedBookmarks: List<NetworkBookmark>) {
        hideLoading()
        checkLearnLensReadiness { if (it) isLearnLensReady = true }
        learnLensHelper?.fetchBookmarks(args.contentId) { bookmarks ->
            pendingBookmarks = bookmarks
            if (hasBookmarksChanged(bookmarks, cachedBookmarks) && bookmarks.isNotEmpty()) {
                injectBookmarksWhenReady(bookmarks)
            }
        }
    }

    private fun loadBookmarksForNewWebView(args: PdfArguments, cachedBookmarks: List<NetworkBookmark>) {
        webView?.let { initializeHelper(it) }
        if (cachedBookmarks.isNotEmpty()) {
            pendingBookmarks = cachedBookmarks
            injectBookmarksWhenReady(cachedBookmarks)
        } else {
            learnLensHelper?.fetchBookmarks(args.contentId) { bookmarks ->
                pendingBookmarks = bookmarks
                if (bookmarks.isNotEmpty()) injectBookmarksWhenReady(bookmarks)
            }
        }
    }
    
    private fun injectBookmarksWhenReady(bookmarks: List<NetworkBookmark>) {
        webView?.let { wv ->
            initializeHelper(wv)
            learnLensHelper?.injectBookmarksWhenReady(bookmarks)
        } ?: run {
            view?.postDelayed({ injectBookmarksWhenReady(bookmarks) }, 500)
        }
    }
    
    private fun hasBookmarksChanged(new: List<NetworkBookmark>, old: List<NetworkBookmark>) =
        new.size != old.size || new.map { it.id }.toSet() != old.map { it.id }.toSet()
    
    private fun initializeHelper(wv: WebView) {
        if (learnLensHelper == null) learnLensHelper = LearnLensHelper(requireContext(), wv)
    }
    
    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun configureWebView(wv: WebView, args: PdfArguments, bookmarks: List<NetworkBookmark>) {
        wv.enableFileAccess()
        wv.webViewClient = BaseWebViewClient(this)
        wv.webChromeClient = webChromeClient
        initializeHelper(wv)
        
        val authToken = TestpressSdk.getTestpressSession(requireContext())?.token ?: ""
        val cacheDir = File(requireContext().filesDir, "web_assets")
        val pdfId = args.learnlensAssetId ?: args.contentId.toString()
        val replacements = learnLensHelper?.buildTemplateReplacements(
            args.pdfUrl, pdfId, authToken, args.pdfTitle, bookmarks
        ) ?: emptyMap()
        
        wv.addJavascriptInterface(LearnLensBridge(requireActivity(), wv, args.contentId), "AndroidBridge")
        wv.loadTemplateAndCacheResources(args.templateName, replacements, "file://${cacheDir.absolutePath}/")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        resetState()
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
    
    override fun onError(exception: TestpressException) = showErrorView(exception)
    override fun isViewActive(): Boolean = isAdded
    
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
                webView?.let { initializeHelper(it); learnLensHelper?.injectBookmarks(bookmarks) }
            }
        }
    }
    
    override fun onLoadingFinished() {
        if (isWebViewCached || isCheckingLearnLens) return
        webView?.let { initializeHelper(it); checkLearnLensReadiness { if (it) isLearnLensReady = true } }
        hideLoading()
        injectPendingAnnotations()
        if (pendingBookmarks == null) webView?.postDelayed({ injectPendingAnnotations() }, 500)
    }
    
    private fun injectPendingAnnotations() {
        pendingBookmarks?.takeIf { it.isNotEmpty() }?.let { injectBookmarksWhenReady(it) }
    }
    
    private data class PdfArguments(
        val contentId: Long,
        val courseId: Long,
        val pdfUrl: String,
        val pdfTitle: String,
        val templateName: String,
        val learnlensAssetId: String?
    )
}
