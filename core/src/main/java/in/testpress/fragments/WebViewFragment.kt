package `in`.testpress.fragments

import `in`.testpress.R
import `in`.testpress.core.*
import `in`.testpress.databinding.WebviewFragmentBinding
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.SSOUrl
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.util.BaseJavaScriptInterface
import `in`.testpress.util.webview.*
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize

class WebViewFragment(
    var url: String = "",
    val data: String = "",
    val webViewFragmentSettings: Settings
) : Fragment(),EmptyViewListener {

    val TAG = "WebViewFragment"
    private var _layout: WebviewFragmentBinding? = null
    private val layout: WebviewFragmentBinding get() = _layout!!
    lateinit var webView: WebView
    lateinit var instituteSettings: InstituteSettings
    private var listener : Listener? = null
    var imagePath: String? = null
    var filePathCallback: ValueCallback<Array<Uri>?>? = null
    private lateinit var emptyViewFragment: EmptyViewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instituteSettings = TestpressSdk.getTestpressSession(requireContext())?.instituteSettings!!
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
        setupCookieManager()
        setupWebView()
        loadContent()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
        _layout = null
    }

    private fun initializedSwipeRefresh(){
        layout.swipeRefreshLayout.isEnabled = webViewFragmentSettings.enableSwipeRefresh
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

    private fun setupCookieManager(){
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.removeAllCookies(null)
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
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.setSupportZoom(webViewFragmentSettings.allowZoomControl)
        webView.webViewClient = CustomWebViewClient(this)
        webView.webChromeClient = CustomWebChromeClient(this)
    }

    private fun loadContent(){
        if (webViewFragmentSettings.isSSORequired){
            fetchSsoLink()
            return
        }
        loadContentInWebView(url = url, data = data)
    }

    private fun fetchSsoLink() {
        showLoading()
        TestpressApiClient(requireContext(), TestpressSdk.getTestpressSession(requireContext())).ssourl
            .enqueue(object : TestpressCallback<SSOUrl>() {
                override fun onSuccess(result: SSOUrl?) {
                    url = "${instituteSettings.baseUrl}${result?.ssoUrl}&next=$url"
                    loadContentInWebView(url = url)
                }

                override fun onException(exception: TestpressException?) {
                    hideLoading()
                    showErrorView(exception!!)
                }
            })
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

    private fun loadContentInWebView(url: String = "", data: String = "") {
        if (url.isNotEmpty()){
            webView.loadUrl(url)
        } else if (data.isNotEmpty()){
            webView.loadData(data,"text/html", null)
        } else {
            // If both the URL and data are empty, pass an unexpected error
            showErrorView(TestpressException.unexpectedError(Exception("URL not found and data not found.")))
        }
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

    @Parcelize
    data class Settings(
        val showLoadingBetweenPages: Boolean = false,
        val isSSORequired: Boolean = true,
        val allowNonInstituteUrlInWebView: Boolean = false,
        val allowZoomControl: Boolean = false,
        val enableSwipeRefresh: Boolean = false
    ) : Parcelable

    interface Listener {
        fun onWebViewInitializationSuccess()
    }

}