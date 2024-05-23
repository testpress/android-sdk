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
        showLoading()
        initializedSwipeRefresh()
        initializeEmptyViewFragment()
        webView = layout.webView
        listener?.onWebViewInitializationSuccess()
        setupWebView()
        populateInstituteSettings()
        loadContent()
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
        webView.webViewClient = CustomWebViewClient(this)
        webView.webChromeClient = CustomWebChromeClient(this)
        webView.settings.userAgentString += CUSTOM_USER_AGENT
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
            webView.loadData(data, "text/html", null)
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