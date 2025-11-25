package `in`.testpress.course.fragments

import `in`.testpress.course.util.WebViewFactory
import `in`.testpress.course.R
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.network.BookmarksListApiResponse
import `in`.testpress.course.network.NetworkBookmark
import `in`.testpress.course.network.NetworkHighlight
import `in`.testpress.course.repository.BookmarkRepository
import `in`.testpress.course.repository.HighlightRepository
import `in`.testpress.course.util.LearnLensBridge
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.util.webview.WebViewEventListener
import `in`.testpress.util.webview.BaseWebViewClient
import `in`.testpress.util.webview.BaseWebChromeClient
import `in`.testpress.util.webview.WebView
import `in`.testpress.v2_4.models.ApiResponse
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
        android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Starting for contentId=${args.contentId}")
        val cacheKey = "pdf_template_${args.contentId}"
        val isNewWebView = !WebViewFactory.isCached(args.contentId, cacheKey)
        android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: isNewWebView=$isNewWebView, cacheKey=$cacheKey")
        
        if (isNewWebView) {
            android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Showing loading (new WebView)")
            showLoading()
        }
        
        // Load WebView immediately with empty annotations (no delay)
        android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Creating WebView")
        webView = WebViewFactory.createCached(
            contentId = args.contentId,
            cacheKey = cacheKey,
            loadUrl = false,
            createWebView = { WebView(requireContext()) }
        ) { wv ->
            android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Configuring WebView with empty annotations")
            configureWebView(wv, args, emptyList(), emptyList())
        }
        
        container?.let { cont -> 
            webView?.let { wv -> 
                android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Attaching WebView to container")
                WebViewFactory.attach(cont, wv)
                wv.requestFocus()
            }
        }
        
        // Hide loading immediately for cached WebViews, or after WebView loads
        if (!isNewWebView) {
            android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Hiding loading (cached WebView)")
            hideLoading()
        }
        
        // Fetch and inject annotations asynchronously (don't block UI)
        android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Starting to fetch initial data")
        fetchInitialData(args) { fetchedHighlights, fetchedBookmarks ->
            // Create immutable copies to avoid smart cast issues
            val highlightsList = fetchedHighlights
            val bookmarksList = fetchedBookmarks
            
            android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Fetched data - highlights=${highlightsList.size}, bookmarks=${bookmarksList.size}")
            android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Highlights: ${highlightsList.map { "id=${it.id}, page=${it.pageNumber}" }}")
            android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Bookmarks: ${bookmarksList.map { "id=${it.id}, page=${it.pageNumber}" }}")
            
            // Store annotations to inject when WebView finishes loading
            pendingHighlights = highlightsList
            pendingBookmarks = bookmarksList
            
            // Try to inject immediately (in case WebView is already loaded)
            android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Attempting to inject annotations immediately")
            injectAnnotations(highlightsList, bookmarksList)
            
            // Only hide loading if it's a new WebView and still showing
            if (isNewWebView && progressBar?.visibility == View.VISIBLE) {
                android.util.Log.d("AIChatPdfFragment", "loadPdfInWebView: Hiding loading after data fetch")
                hideLoading()
            }
        }
    }
    
    private fun injectAnnotations(highlights: List<NetworkHighlight>, bookmarks: List<NetworkBookmark>) {
        android.util.Log.d("AIChatPdfFragment", "injectAnnotations: Starting - highlights=${highlights.size}, bookmarks=${bookmarks.size}")
        webView?.let { wv ->
            // Use snake_case Gson for LearnLens compatibility
            val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
            
            // Convert highlights to snake_case format for LearnLens
            val highlightsForLearnLens = highlights.map { highlight ->
                mapOf(
                    "id" to (highlight.id ?: 0L),
                    "page_number" to (highlight.pageNumber ?: 0),
                    "selected_text" to (highlight.selectedText ?: ""),
                    "notes" to (highlight.notes ?: ""),
                    "color" to (highlight.color ?: "#FFEB3B"),
                    "position" to (highlight.position ?: emptyList())
                )
            }
            
            // Convert bookmarks to snake_case format for LearnLens
            val bookmarksForLearnLens = bookmarks.map { bookmark ->
                mapOf(
                    "id" to (bookmark.id ?: 0L),
                    "page_number" to (bookmark.pageNumber ?: 0),
                    "preview_text" to (bookmark.previewText ?: "")
                )
            }
            
            val highlightsJson = gson.toJson(highlightsForLearnLens)
            val bookmarksJson = gson.toJson(bookmarksForLearnLens)
            
            android.util.Log.d("AIChatPdfFragment", "injectAnnotations: Highlights JSON length=${highlightsJson.length}, Bookmarks JSON length=${bookmarksJson.length}")
            android.util.Log.d("AIChatPdfFragment", "injectAnnotations: Highlights JSON preview: ${highlightsJson.take(200)}")
            android.util.Log.d("AIChatPdfFragment", "injectAnnotations: Bookmarks JSON preview: ${bookmarksJson.take(200)}")
            
            // Inject annotations into LearnLens using the helper function
            // This will either apply them immediately if LearnLens is ready, or store them for initialization
            val script = """
                (function injectAnnotations() {
                    console.log('AIChatPdfFragment: injectAnnotations called');
                    console.log('AIChatPdfFragment: Highlights count:', $highlightsJson.length);
                    console.log('AIChatPdfFragment: Bookmarks count:', $bookmarksJson.length);
                    
                    var highlights = $highlightsJson;
                    var bookmarks = $bookmarksJson;
                    
                    // Use the helper function that handles both initialization and updates
                    if (window.setAnnotationsForLearnLens) {
                        console.log('AIChatPdfFragment: Using setAnnotationsForLearnLens helper');
                        window.setAnnotationsForLearnLens(highlights, bookmarks);
                    } else if (window.LearnLens && window.LearnLens.setAnnotations) {
                        // Fallback: direct call if helper not available
                        console.log('AIChatPdfFragment: Helper not available, calling setAnnotations directly');
                        try {
                            window.LearnLens.setAnnotations(highlights, bookmarks);
                            console.log('AIChatPdfFragment: Annotations injected successfully');
                        } catch (e) {
                            console.error('AIChatPdfFragment: Error injecting annotations', e);
                        }
                    } else {
                        console.log('AIChatPdfFragment: LearnLens not ready, storing annotations for later');
                        // Store annotations and retry
                        var attempts = 0;
                        var maxAttempts = 20; // Increased to 10 seconds
                        var interval = setInterval(function() {
                            attempts++;
                            if (window.setAnnotationsForLearnLens) {
                                clearInterval(interval);
                                window.setAnnotationsForLearnLens(highlights, bookmarks);
                                console.log('AIChatPdfFragment: Annotations applied after retry');
                            } else if (window.LearnLens && window.LearnLens.setAnnotations) {
                                clearInterval(interval);
                                try {
                                    window.LearnLens.setAnnotations(highlights, bookmarks);
                                    console.log('AIChatPdfFragment: Annotations injected after retry');
                                } catch (e) {
                                    console.error('AIChatPdfFragment: Error injecting annotations', e);
                                }
                            } else if (attempts >= maxAttempts) {
                                clearInterval(interval);
                                console.warn('AIChatPdfFragment: Failed to inject annotations after ' + maxAttempts + ' attempts');
                            }
                        }, 500);
                    }
                })();
            """.trimIndent()
            
            android.util.Log.d("AIChatPdfFragment", "injectAnnotations: Executing JavaScript injection")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                wv.evaluateJavascript(script, null)
            } else {
                wv.loadUrl("javascript:$script")
            }
        } ?: android.util.Log.w("AIChatPdfFragment", "injectAnnotations: WebView is null, cannot inject")
    }
    
    private fun fetchInitialData(
        args: PdfArguments,
        callback: (List<NetworkHighlight>, List<NetworkBookmark>) -> Unit
    ) {
        android.util.Log.d("AIChatPdfFragment", "fetchInitialData: Starting for contentId=${args.contentId}")
        val highlightRepository = HighlightRepository(requireContext())
        val bookmarkRepository = BookmarkRepository(requireContext())
        
        var highlights: List<NetworkHighlight>? = null
        var bookmarks: List<NetworkBookmark>? = null
        var highlightsLoaded = false
        var bookmarksLoaded = false
        
        val startTime = System.currentTimeMillis()
        
        fun checkAndCallback() {
            if (highlightsLoaded && bookmarksLoaded) {
                val elapsedTime = System.currentTimeMillis() - startTime
                // Create immutable copies to avoid smart cast issues
                val highlightsList = highlights ?: emptyList()
                val bookmarksList = bookmarks ?: emptyList()
                android.util.Log.d("AIChatPdfFragment", "fetchInitialData: Both requests completed in ${elapsedTime}ms")
                android.util.Log.d("AIChatPdfFragment", "fetchInitialData: Calling callback with highlights=${highlightsList.size}, bookmarks=${bookmarksList.size}")
                callback(highlightsList, bookmarksList)
            } else {
                android.util.Log.d("AIChatPdfFragment", "fetchInitialData: Waiting for both requests - highlightsLoaded=$highlightsLoaded, bookmarksLoaded=$bookmarksLoaded")
            }
        }
        
        android.util.Log.d("AIChatPdfFragment", "fetchInitialData: Requesting highlights")
        highlightRepository.getHighlights(args.contentId, object : TestpressCallback<ApiResponse<List<NetworkHighlight>>>() {
            override fun onSuccess(response: ApiResponse<List<NetworkHighlight>>) {
                val highlightsList = response.results ?: emptyList()
                highlights = highlightsList
                highlightsLoaded = true
                android.util.Log.d("AIChatPdfFragment", "fetchInitialData: Highlights loaded - count=${highlightsList.size}")
                checkAndCallback()
            }
            
            override fun onException(exception: TestpressException?) {
                android.util.Log.e("AIChatPdfFragment", "fetchInitialData: Failed to load highlights: ${exception?.message}", exception)
                highlights = emptyList()
                highlightsLoaded = true
                checkAndCallback()
            }
        })
        
        val bookmarkQueryParams = hashMapOf<String, Any>(
            "content_type" to "chapter_content",
            "object_id" to args.contentId,
            "bookmark_type" to "annotate"
        )
        
        android.util.Log.d("AIChatPdfFragment", "fetchInitialData: Requesting bookmarks with params: $bookmarkQueryParams")
        bookmarkRepository.getBookmarks(bookmarkQueryParams, object : TestpressCallback<ApiResponse<BookmarksListApiResponse>>() {
            override fun onSuccess(response: ApiResponse<BookmarksListApiResponse>) {
                val bookmarksList = response.results?.bookmarks ?: emptyList()
                bookmarks = bookmarksList
                bookmarksLoaded = true
                android.util.Log.d("AIChatPdfFragment", "fetchInitialData: Bookmarks loaded - count=${bookmarksList.size}")
                checkAndCallback()
            }
            
            override fun onException(exception: TestpressException?) {
                android.util.Log.e("AIChatPdfFragment", "fetchInitialData: Failed to load bookmarks: ${exception?.message}", exception)
                bookmarks = emptyList()
                bookmarksLoaded = true
                checkAndCallback()
            }
        })
    }
    
    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun configureWebView(
        wv: WebView,
        args: PdfArguments,
        highlights: List<NetworkHighlight>,
        bookmarks: List<NetworkBookmark>
    ) {
        android.util.Log.d("AIChatPdfFragment", "configureWebView: Starting - highlights=${highlights.size}, bookmarks=${bookmarks.size}")
        wv.enableFileAccess()
        wv.webViewClient = BaseWebViewClient(this)
        wv.webChromeClient = webChromeClient
        
        val authToken = TestpressSdk.getTestpressSession(requireContext())?.token ?: ""
        val cacheDir = File(requireContext().filesDir, "web_assets")
        val pdfId = args.learnlensAssetId ?: args.contentId.toString()
        
        // Use snake_case Gson for LearnLens compatibility
        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
        
        // Convert to snake_case format for LearnLens
        val highlightsForLearnLens = highlights.map { highlight ->
            mapOf(
                "id" to (highlight.id ?: 0L),
                "page_number" to (highlight.pageNumber ?: 0),
                "selected_text" to (highlight.selectedText ?: ""),
                "notes" to (highlight.notes ?: ""),
                "color" to (highlight.color ?: "#FFEB3B"),
                "position" to (highlight.position ?: emptyList())
            )
        }
        
        val bookmarksForLearnLens = bookmarks.map { bookmark ->
            mapOf(
                "id" to (bookmark.id ?: 0L),
                "page_number" to (bookmark.pageNumber ?: 0),
                "preview_text" to (bookmark.previewText ?: "")
            )
        }
        
        val highlightsJson = gson.toJson(highlightsForLearnLens)
        val bookmarksJson = gson.toJson(bookmarksForLearnLens)
        
        android.util.Log.d("AIChatPdfFragment", "configureWebView: Initial highlights JSON: ${highlightsJson.take(200)}")
        android.util.Log.d("AIChatPdfFragment", "configureWebView: Initial bookmarks JSON: ${bookmarksJson.take(200)}")
        
        wv.addJavascriptInterface(
            LearnLensBridge(requireActivity(), wv, args.contentId),
            "AndroidBridge"
        )
        
        android.util.Log.d("AIChatPdfFragment", "configureWebView: Loading template with PDF_URL=${args.pdfUrl}, PDF_ID=$pdfId")
        wv.loadTemplateAndCacheResources(
            templateName = args.templateName,
            replacements = mapOf(
                "PDF_URL" to args.pdfUrl,
                "PDF_ID" to pdfId,
                "AUTH_TOKEN" to authToken,
                "PDF_TITLE" to args.pdfTitle,
                "INITIAL_HIGHLIGHTS_JSON" to highlightsJson,
                "INITIAL_BOOKMARKS_JSON" to bookmarksJson
            ),
            baseUrl = "file://${cacheDir.absolutePath}/"
        )
        android.util.Log.d("AIChatPdfFragment", "configureWebView: Template loading initiated")
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
    
    private var pendingHighlights: List<NetworkHighlight>? = null
    private var pendingBookmarks: List<NetworkBookmark>? = null
    
    override fun onLoadingStarted() {
        android.util.Log.d("AIChatPdfFragment", "onLoadingStarted: WebView started loading")
        showLoading()
    }
    
    override fun onLoadingFinished() {
        android.util.Log.d("AIChatPdfFragment", "onLoadingFinished: WebView finished loading")
        
        // Don't hide loading immediately - wait for LearnLens to initialize
        // Check if LearnLens is ready, if not wait a bit
        webView?.let { wv ->
            checkLearnLensAndHideLoading(wv)
        } ?: run {
            // No WebView, just hide loading
            hideLoading()
            injectPendingAnnotations()
        }
    }
    
    private fun checkLearnLensAndHideLoading(wv: WebView, attempts: Int = 0) {
        if (attempts >= 30) { // Max 3 seconds (30 * 100ms)
            android.util.Log.w("AIChatPdfFragment", "onLoadingFinished: LearnLens not ready after timeout, hiding loading anyway")
            hideLoading()
            injectPendingAnnotations()
            return
        }
        
        val checkLearnLensReady = """
            (function() {
                if (window.LearnLens && document.getElementById('learnlens-pdf-chat') && document.getElementById('learnlens-pdf-chat').children.length > 0) {
                    return true;
                }
                return false;
            })();
        """.trimIndent()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wv.evaluateJavascript(checkLearnLensReady) { result ->
                if (result == "true") {
                    android.util.Log.d("AIChatPdfFragment", "onLoadingFinished: LearnLens ready after ${attempts * 100}ms, hiding loading")
                    hideLoading()
                    injectPendingAnnotations()
                } else {
                    android.util.Log.d("AIChatPdfFragment", "onLoadingFinished: LearnLens not ready yet (attempt ${attempts + 1}/30), checking again...")
                    wv.postDelayed({
                        checkLearnLensAndHideLoading(wv, attempts + 1)
                    }, 100)
                }
            }
        } else {
            // Fallback for older Android versions
            wv.postDelayed({
                hideLoading()
                injectPendingAnnotations()
            }, 500)
        }
    }
    
    private fun injectPendingAnnotations() {
        // If we have pending annotations, inject them now that WebView is loaded
        pendingHighlights?.let { highlights ->
            pendingBookmarks?.let { bookmarks ->
                android.util.Log.d("AIChatPdfFragment", "onLoadingFinished: Injecting pending annotations - highlights=${highlights.size}, bookmarks=${bookmarks.size}")
                injectAnnotations(highlights, bookmarks)
                pendingHighlights = null
                pendingBookmarks = null
            } ?: android.util.Log.w("AIChatPdfFragment", "onLoadingFinished: Pending highlights but no bookmarks")
        } ?: android.util.Log.d("AIChatPdfFragment", "onLoadingFinished: No pending annotations")
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
