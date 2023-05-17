package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.databinding.TestpressContainerLayoutBinding
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.SSOUrl
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.util.ActivityUtil
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import kotlinx.parcelize.Parcelize
import java.io.IOException

class WebViewWithSSO1: BaseToolBarActivity(), EmptyViewListener, WebViewFragment.Listener {

    private var _binding: TestpressContainerLayoutBinding? = null
    private val binding: TestpressContainerLayoutBinding get() = _binding!!
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var webViewFragment: WebViewFragment
    private lateinit var instituteSettings: InstituteSettings
    private var isUrlAvailable : Boolean = false
    private lateinit var title : String
    private var url : String? = null
    private var urlPath : String? = null
    private var showLoadingBetweenPages = false
    private lateinit var webViewSettings: Settings
    private var isSSORequired: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = TestpressContainerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        instituteSettings = TestpressSdk.getTestpressSession(this)?.instituteSettings!!
        parseArguments()
        setActionBarTitle(title)
        initializeEmptyViewFragment()
        initializeWebViewFragment()
        showLoading()
        openWebView()
    }

    override fun onBackPressed() {
        if (webViewFragment.canGoBack()){
            webViewFragment.goBack()
        }else {
            super.onBackPressed()
        }
    }

    private fun parseArguments() {
        isUrlAvailable = intent.getBooleanExtra(IS_URL_AVAILABLE,false)
        title = intent.getStringExtra(TITLE)?: instituteSettings.appName
        url = intent.getStringExtra(URL_TO_OPEN)
        urlPath = intent.getStringExtra(URL_PATH_TO_OPEN)
        webViewSettings = intent.getParcelableExtra(WEB_VIEW_SETTINGS)!!
        showLoadingBetweenPages = webViewSettings.showLoadingBetweenPages
        isSSORequired = webViewSettings.IsSSORequired
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, emptyViewFragment)
        transaction.commit()
    }

    private fun initializeWebViewFragment() {
        webViewFragment = WebViewFragment()
        webViewFragment.setListener(this)
    }

    private fun showLoading() {
        binding.pbLoading.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
    }

    private fun openWebView() {
        if (isSSORequired) {
            fetchSsoLink()
        } else {
            if (!isInstituteURL(url!!) && webViewSettings.allowNonInstituteUrlInWebView){
                loadWebViewFragment()
            } else {
                ActivityUtil.openUrl(this,url!!)
                this.finish()
            }
        }
    }

    private fun fetchSsoLink() {
        TestpressApiClient(this, TestpressSdk.getTestpressSession(this)).ssourl
            .enqueue(object : TestpressCallback<SSOUrl>() {
                override fun onSuccess(result: SSOUrl?) {
                    loadWebViewFragment(result)
                }

                override fun onException(exception: TestpressException?) {
                    hideLoading()
                    showErrorView(exception)
                }
            })
    }

    private fun loadWebViewFragment(ssoLink: SSOUrl? = null) {
        val urlToOpen = if (isSSORequired) getSSOUrlToOpen(ssoLink) else getUrlToOpen()
        val arguments = Bundle().apply { putString(WebViewFragment.URL_TO_OPEN, urlToOpen) }
        webViewFragment.arguments = arguments
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, webViewFragment)
            .commit()
    }

    private fun getSSOUrlToOpen(ssoLink: SSOUrl?):String{
        return if (isUrlAvailable){
            "$url${ssoLink?.ssoUrl}"
        } else {
            "${instituteSettings.baseUrl}${ssoLink?.ssoUrl}&next=$urlPath"
        }
    }

    private fun getUrlToOpen():String{
        return if (isUrlAvailable){
            url!!
        } else {
            "${instituteSettings.baseUrl}$urlPath"
        }
    }

    private fun hideLoading() {
        binding.pbLoading.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
    }

    private fun showErrorView(exception: java.lang.Exception?) {
        if (exception?.cause is IOException) {
            val testpressException = TestpressException.networkError(exception.cause as IOException)
            emptyViewFragment.displayError(testpressException)
        } else {
            val testpressException = TestpressException.unexpectedError(exception)
            emptyViewFragment.displayError(testpressException)
        }
    }

    override fun onRetryClick() {
        showLoading()
        openWebView()
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return if (isInstituteURL(request?.url.toString())) {
            false
        } else {
            !webViewSettings.allowNonInstituteUrlInWebView
        }
    }

    private fun isInstituteURL(url: String): Boolean {
        return url.contains(instituteSettings.baseUrl)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (showLoadingBetweenPages){
            showLoading()
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        hideLoading()
    }

    companion object {
        private const val IS_URL_AVAILABLE = "IS_URL_AVAILABLE"
        const val TITLE = "TITLE"
        const val URL_TO_OPEN = "URL"
        const val URL_PATH_TO_OPEN = "URL_PATH"
        const val WEB_VIEW_SETTINGS = "WEB_VIEW_SETTINGS"

        @JvmStatic
        fun createUrlIntent(
            context: Context,
            title: String,
            url: String,
            webViewSettings: Settings,
        ): Intent {
            return Intent(context, WebViewWithSSO1::class.java).apply {
                putExtra(IS_URL_AVAILABLE, true)
                putExtra(TITLE, title)
                putExtra(URL_TO_OPEN, url)
                putExtra(WEB_VIEW_SETTINGS,webViewSettings)
            }
        }

        @JvmStatic
        fun createUrlPathIntent(
            context: Context,
            title: String,
            urlPath: String,
            webViewSettings: Settings
        ): Intent {
            return Intent(context, WebViewWithSSO1::class.java).apply {
                putExtra(IS_URL_AVAILABLE, false)
                putExtra(TITLE, title)
                putExtra(URL_PATH_TO_OPEN, urlPath)
                putExtra(WEB_VIEW_SETTINGS,webViewSettings)
            }
        }
    }

    @Parcelize
    data class Settings(
        val showLoadingBetweenPages: Boolean = false,
        val IsSSORequired: Boolean = true,
        val allowNonInstituteUrlInWebView: Boolean = true
    ) : Parcelable
}