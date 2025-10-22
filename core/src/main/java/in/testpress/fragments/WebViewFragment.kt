package `in`.testpress.fragments

import `in`.testpress.R
import `in`.testpress.core.*
import `in`.testpress.databinding.WebviewFragmentBinding
import `in`.testpress.models.InstituteSettings
import `in`.testpress.util.BaseJavaScriptInterface
import `in`.testpress.util.UserAgentProvider
import `in`.testpress.util.webview.*
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment

const val CUSTOM_USER_AGENT = " TestpressAndroidApp/WebView"

class WebViewFragment : Fragment(), EmptyViewListener {

    val TAG = "WebViewFragment"
    private var _layout: WebviewFragmentBinding? = null
    private val layout: WebviewFragmentBinding get() = _layout!!
    lateinit var webView: WebView
    var instituteSettings: InstituteSettings? = null
    private var listener : Listener? = null
    var imagePath: String? = null
    var filePathCallback: ValueCallback<Array<Uri>?>? = null
    private lateinit var emptyViewFragment: EmptyViewFragment
    private var url: String = ""
    private var data: String = ""
    var showLoadingBetweenPages: Boolean = false
    private var isAuthenticationRequired: Boolean = true
    var allowNonInstituteUrlInWebView: Boolean = false
    private var allowZoomControl: Boolean = false
    private var enableSwipeRefresh: Boolean = false
    var session: TestpressSession? = null
    var lockToLandscape: Boolean = false
    private var webViewClient: CustomWebViewClient? = null
    var loadUrlCalledTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startTime = System.currentTimeMillis()
        android.util.Log.d("AI_TIMING", "🟦 STEP 8: WebViewFragment.onCreate() STARTED")
        
        parseArguments()
        
        android.util.Log.d("AI_TIMING", "✅ STEP 8 DONE: WebViewFragment.onCreate() completed in ${System.currentTimeMillis() - startTime}ms")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val startTime = System.currentTimeMillis()
        android.util.Log.d("AI_TIMING", "🟦 STEP 9: WebViewFragment.onCreateView() STARTED")
        
        _layout = WebviewFragmentBinding.inflate(inflater, container, false)
        
        android.util.Log.d("AI_TIMING", "✅ STEP 9 DONE: WebViewFragment.onCreateView() completed in ${System.currentTimeMillis() - startTime}ms")
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val startTime = System.currentTimeMillis()
        android.util.Log.d("AI_TIMING", "🟦 STEP 10: WebViewFragment.onViewCreated() STARTED")
        
        showLoading()
        android.util.Log.d("AI_TIMING", "   Showing loading spinner... (${System.currentTimeMillis() - startTime}ms)")
        
        initializedSwipeRefresh()
        android.util.Log.d("AI_TIMING", "   Initialized swipe refresh (${System.currentTimeMillis() - startTime}ms)")
        
        initializeEmptyViewFragment()
        android.util.Log.d("AI_TIMING", "   Initialized empty view fragment (${System.currentTimeMillis() - startTime}ms)")
        
        webView = layout.webView
        listener?.onWebViewInitializationSuccess()
        android.util.Log.d("AI_TIMING", "   WebView reference obtained (${System.currentTimeMillis() - startTime}ms)")
        
        android.util.Log.d("AI_TIMING", "🟦 STEP 11: Calling setupWebView()...")
        val setupStart = System.currentTimeMillis()
        setupWebView()
        android.util.Log.d("AI_TIMING", "✅ STEP 11 DONE: setupWebView() completed in ${System.currentTimeMillis() - setupStart}ms")
        
        android.util.Log.d("AI_TIMING", "🟦 STEP 12: Calling populateInstituteSettings()...")
        val settingsStart = System.currentTimeMillis()
        populateInstituteSettings()
        android.util.Log.d("AI_TIMING", "✅ STEP 12 DONE: populateInstituteSettings() completed in ${System.currentTimeMillis() - settingsStart}ms")
        
        android.util.Log.d("AI_TIMING", "🟦 STEP 13: Calling loadContent() - NETWORK REQUEST STARTS HERE...")
        val loadStart = System.currentTimeMillis()
        loadContent()
        android.util.Log.d("AI_TIMING", "✅ STEP 13 DONE: loadContent() initiated in ${System.currentTimeMillis() - loadStart}ms (network request continues in background)")
        
        android.util.Log.d("AI_TIMING", "✅ STEP 10 DONE: WebViewFragment.onViewCreated() completed in ${System.currentTimeMillis() - startTime}ms total")
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
        _layout = null
    }

    private fun parseArguments() {
        url = arguments?.getString(URL_TO_OPEN, "") ?: ""
        data = arguments?.getString(DATA_TO_OPEN, "") ?: ""
        showLoadingBetweenPages = arguments?.getBoolean(SHOW_LOADING_BETWEEN_PAGES) ?: false
        isAuthenticationRequired = arguments?.getBoolean(IS_AUTHENTICATION_REQUIRED) ?: true
        allowNonInstituteUrlInWebView =
            arguments?.getBoolean(ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW) ?: false
        allowZoomControl = arguments?.getBoolean(ALLOW_ZOOM_CONTROLS) ?: false
        enableSwipeRefresh = arguments?.getBoolean(ENABLE_SWIPE_REFRESH) ?: false
    }

    private fun initializedSwipeRefresh(){
        layout.swipeRefreshLayout.isEnabled = enableSwipeRefresh
        layout.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.testpress_color_primary),
        )
        layout.swipeRefreshLayout.setOnRefreshListener {
            webView.loadUrl(webView.url.toString())
        }
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.useWideViewPort = false
        webView.settings.loadWithOverviewMode = true
        // Allow use of Local Storage
        webView.settings.domStorageEnabled = true
        // Disable pinch to zoom without the zoom buttons
        webView.settings.builtInZoomControls = false
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.setSupportZoom(allowZoomControl)
        
        // Set WebView clients
        val webViewClient = CustomWebViewClient(this)
        webView.webViewClient = webViewClient
        webView.webChromeClient = CustomWebChromeClient(this)
        
        // Store reference to client for timing
        this.webViewClient = webViewClient
        
        webView.settings.userAgentString += CUSTOM_USER_AGENT
        webView.apply {
            clearCache(true)
            clearHistory()
        }
    }

    private fun populateInstituteSettings() {
        session = TestpressSdk.getTestpressSession(requireContext())
        instituteSettings = session?.instituteSettings
        checkNotNull(instituteSettings) { "InstituteSettings must not be null" }
    }

    fun showLoading() {
        layout.pbLoading.visibility = View.VISIBLE
        layout.webView.visibility = View.GONE
    }

    fun hideLoading() {
        layout.swipeRefreshLayout.isRefreshing = false
        layout.pbLoading.visibility = View.GONE
        layout.webView.visibility = View.VISIBLE
    }

    private fun loadContent() {
        android.util.Log.d("AI_TIMING", "")
        android.util.Log.d("AI_TIMING", "=== DETAILED loadContent() ANALYSIS ===")
        val loadContentStart = System.currentTimeMillis()
        
        if (url.isNotEmpty()) {
            android.util.Log.d("AI_TIMING", "📍 URL to load: $url")
            
            // Generate headers
            val headersStart = System.currentTimeMillis()
            val headers = generateHeadersMap()
            android.util.Log.d("AI_TIMING", "⏱️  generateHeadersMap() took: ${System.currentTimeMillis() - headersStart}ms")
            
            // Log headers
            android.util.Log.d("AI_TIMING", "📋 Headers being sent:")
            headers.forEach { (key, value) ->
                if (key == "Authorization") {
                    android.util.Log.d("AI_TIMING", "   $key: JWT [token hidden for security]")
                } else {
                    android.util.Log.d("AI_TIMING", "   $key: $value")
                }
            }
            
            // Actually load the URL
            android.util.Log.d("AI_TIMING", "🌐 Calling webView.loadUrl()...")
            android.util.Log.d("AI_TIMING", "")
            
            val webViewLoadStart = System.currentTimeMillis()
            loadUrlCalledTime = webViewLoadStart  // Store for timing calculations
            webViewClient?.setLoadUrlTime(webViewLoadStart)  // Pass to client
            
            // 🧪 TEST MODE: Load simple HTML instead of network URL
            android.util.Log.d("AI_TIMING", "🧪 TEST MODE: Loading local HTML instead of network URL")
            android.util.Log.d("AI_TIMING", "   This will tell us if delay is from network or WebView initialization")
            android.util.Log.d("AI_TIMING", "")
            
            val testHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Hello World Test</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            margin: 0;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                        }
                        .container {
                            text-align: center;
                            padding: 40px;
                            background: rgba(255, 255, 255, 0.1);
                            border-radius: 20px;
                            backdrop-filter: blur(10px);
                        }
                        h1 {
                            font-size: 48px;
                            margin: 0 0 20px 0;
                        }
                        p {
                            font-size: 18px;
                            opacity: 0.9;
                        }
                        .info {
                            margin-top: 30px;
                            font-size: 14px;
                            opacity: 0.7;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🚀 Hello World!</h1>
                        <p>WebView loaded successfully</p>
                        <div class="info">
                            <p>✅ No network request</p>
                            <p>✅ Pure local HTML</p>
                            <p>✅ Instant loading test</p>
                        </div>
                    </div>
                    <script>
                        console.log('Hello World HTML loaded at:', new Date().toISOString());
                    </script>
                </body>
                </html>
            """.trimIndent()
            
            webView.loadDataWithBaseURL(
                null,  // baseUrl
                testHtml,  // data
                "text/html",  // mimeType
                "UTF-8",  // encoding
                null  // historyUrl
            )
            
            // Original network URL loading (commented out for test)
            // webView.loadUrl(url, headers)
            
            android.util.Log.d("AI_TIMING", "⏱️  webView.loadDataWithBaseURL() call returned in: ${System.currentTimeMillis() - webViewLoadStart}ms")
            android.util.Log.d("AI_TIMING", "")
            android.util.Log.d("AI_TIMING", "⚠️  IMPORTANT: loadDataWithBaseURL() returns immediately!")
            android.util.Log.d("AI_TIMING", "   Since this is local HTML, there's NO network delay")
            android.util.Log.d("AI_TIMING", "   Any delay you see is purely from WebView rendering")
            android.util.Log.d("AI_TIMING", "")
            android.util.Log.d("AI_TIMING", "   ⏰ Waiting for onPageStarted() callback...")
            android.util.Log.d("AI_TIMING", "   (Should fire almost instantly with local HTML)")
            
        } else if (data.isNotEmpty()) {
            webView.loadData(data, "text/html", null)
        } else {
            // If both the URL and data are empty, pass an unexpected error
            showErrorView(TestpressException.unexpectedError(Exception("URL not found and data not found.")))
        }
        
        android.util.Log.d("AI_TIMING", "⏱️  Total loadContent() execution: ${System.currentTimeMillis() - loadContentStart}ms")
        android.util.Log.d("AI_TIMING", "=====================================")
        android.util.Log.d("AI_TIMING", "")
    }

    private fun generateHeadersMap(): Map<String, String> {
        val headersMap = mutableMapOf<String, String>()
        if (isAuthenticationRequired){
            val tokenStart = System.currentTimeMillis()
            val token = session?.token
            android.util.Log.d("AI_TIMING", "   Getting auth token took: ${System.currentTimeMillis() - tokenStart}ms")
            
            val userAgentStart = System.currentTimeMillis()
            val userAgent = UserAgentProvider.get(requireContext())
            android.util.Log.d("AI_TIMING", "   Getting user agent took: ${System.currentTimeMillis() - userAgentStart}ms")
            
            headersMap["Authorization"] = "JWT $token"
            headersMap["User-Agent"] = userAgent
        }
        return headersMap
    }

    fun showErrorView(exception: TestpressException) {
        hideWebViewShowEmptyView()
        emptyViewFragment.displayError(exception)
    }

    fun hideEmptyViewShowWebView() {
        layout.emptyViewContainer.isVisible = false
        layout.webView.isVisible = true
    }

    private fun hideWebViewShowEmptyView() {
        layout.emptyViewContainer.isVisible = true
        layout.webView.isVisible = false
    }

    override fun onRetryClick() {
        retryLoad()
    }

    fun setListener(listener: Listener){
        this.listener = listener
    }

    @SuppressLint("JavascriptInterface")
    fun addJavascriptInterface(javascriptInterface: BaseJavaScriptInterface, name: String){
        webView.addJavascriptInterface(javascriptInterface,name)
    }

    fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    fun goBack() {
        webView.goBack()
    }

    private fun retryLoad(){
        hideEmptyViewShowWebView()
        showLoading()
        if (!webView.url.isNullOrEmpty()){
            webView.loadUrl(webView.url!!)
        } else {
            loadContent()
        }
    }

    fun isInstituteUrl(url: String?) = url != null && instituteSettings?.isInstituteUrl(url) == true

    interface Listener {
        fun onWebViewInitializationSuccess()
    }

    companion object {
        const val URL_TO_OPEN = "URL_TO_OPEN"
        const val DATA_TO_OPEN = "DATA_TO_OPEN"
        const val IS_AUTHENTICATION_REQUIRED = "IS_AUTHENTICATION_REQUIRED"
        const val SHOW_LOADING_BETWEEN_PAGES = "SHOW_LOADING_BETWEEN_PAGES"
        const val ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW = "ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW"
        const val ALLOW_ZOOM_CONTROLS = "ALLOW_ZOOM_CONTROLS"
        const val ENABLE_SWIPE_REFRESH = "ENABLE_SWIPE_REFRESH"
    }

}