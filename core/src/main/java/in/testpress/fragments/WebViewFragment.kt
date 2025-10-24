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
import java.io.File

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _layout = WebviewFragmentBinding.inflate(inflater, container, false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val startTime = System.currentTimeMillis()
        android.util.Log.d(TAG, "⏱️ ================================================")
        android.util.Log.d(TAG, "⏱️ WebViewFragment.onViewCreated() STARTED")
        android.util.Log.d(TAG, "⏱️ ================================================")
        
        val loadingStart = System.currentTimeMillis()
        showLoading()
        android.util.Log.d(TAG, "⏱️ showLoading() took: ${System.currentTimeMillis() - loadingStart}ms")
        
        val refreshStart = System.currentTimeMillis()
        initializedSwipeRefresh()
        android.util.Log.d(TAG, "⏱️ initializedSwipeRefresh() took: ${System.currentTimeMillis() - refreshStart}ms")
        
        val emptyViewStart = System.currentTimeMillis()
        initializeEmptyViewFragment()
        android.util.Log.d(TAG, "⏱️ initializeEmptyViewFragment() took: ${System.currentTimeMillis() - emptyViewStart}ms")
        
        val webViewRefStart = System.currentTimeMillis()
        webView = layout.webView
        android.util.Log.d(TAG, "⏱️ WebView reference obtained in: ${System.currentTimeMillis() - webViewRefStart}ms")
        
        listener?.onWebViewInitializationSuccess()
        
        val setupStart = System.currentTimeMillis()
        setupWebView()
        android.util.Log.d(TAG, "⏱️ setupWebView() took: ${System.currentTimeMillis() - setupStart}ms")
        
        val populateStart = System.currentTimeMillis()
        populateInstituteSettings()
        android.util.Log.d(TAG, "⏱️ populateInstituteSettings() took: ${System.currentTimeMillis() - populateStart}ms")
        
        val loadContentStart = System.currentTimeMillis()
        loadContent()
        android.util.Log.d(TAG, "⏱️ loadContent() call took: ${System.currentTimeMillis() - loadContentStart}ms")
        
        val totalTime = System.currentTimeMillis() - startTime
        android.util.Log.d(TAG, "⏱️ Total WebViewFragment.onViewCreated() time: ${totalTime}ms")
        android.util.Log.d(TAG, "⏱️ ================================================")
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
        webView.settings.allowFileAccessFromFileURLs = true  // Allow loading file:// resources
        webView.settings.allowUniversalAccessFromFileURLs = true  // Allow cross-origin file:// requests
        webView.settings.useWideViewPort = false
        webView.settings.loadWithOverviewMode = true
        // Allow use of Local Storage
        webView.settings.domStorageEnabled = true
        // Disable pinch to zoom without the zoom buttons
        webView.settings.builtInZoomControls = false
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.setSupportZoom(allowZoomControl)
        webView.webViewClient = CustomWebViewClient(this)
        webView.webChromeClient = CustomWebChromeClient(this)
        webView.settings.userAgentString += CUSTOM_USER_AGENT
        
        // Enable WebView debugging to see console logs
        WebView.setWebContentsDebuggingEnabled(true)
        
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
        if (url.isNotEmpty()) {
            webView.loadUrl(url, generateHeadersMap())
        } else if (data.isNotEmpty()) {
            // Use loadDataWithBaseURL for better HTML rendering
            android.util.Log.d(TAG, "📄 Loading HTML data (${data.length} chars)")
            
            // IMPORTANT: Use cache directory as base URL to allow loading local JS/CSS files
            // This fixes CORS issue where script imports are blocked
            val cacheDir = File(requireContext().filesDir, "learnlens_cache")
            val baseUrl = "file://${cacheDir.absolutePath}/"
            webView.loadDataWithBaseURL(baseUrl, data, "text/html", "UTF-8", null)
            
            android.util.Log.d(TAG, "✅ HTML loaded with base URL: $baseUrl")
            
        } else {
            // If both the URL and data are empty, pass an unexpected error
            showErrorView(TestpressException.unexpectedError(Exception("URL not found and data not found.")))
        }
    }

    private fun generateHeadersMap(): Map<String, String> {
        val headersMap = mutableMapOf<String, String>()
        if (isAuthenticationRequired){
            headersMap["Authorization"] = "JWT ${session?.token}"
            headersMap["User-Agent"] = UserAgentProvider.get(requireContext())
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