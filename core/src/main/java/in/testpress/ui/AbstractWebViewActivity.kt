package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.databinding.BaseTestpressWebviewContainerLayoutBinding
import `in`.testpress.fragments.WebViewFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle

abstract class AbstractWebViewActivity: BaseToolBarActivity(), WebViewFragment.Listener {

    private var _layout: BaseTestpressWebviewContainerLayoutBinding? = null
    private val layout: BaseTestpressWebviewContainerLayoutBinding get() = _layout!!
    protected lateinit var webViewFragment: WebViewFragment
    private lateinit var title: String
    private lateinit var urlPath: String
    private var isAuthenticationRequired: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _layout = BaseTestpressWebviewContainerLayoutBinding.inflate(layoutInflater)
        setContentView(layout.root)
        parseArguments()
        setActionBarTitle(title)
        initializeWebViewFragment()
    }

    override fun onBackPressed() {
        if (webViewFragment.canGoBack()) {
            webViewFragment.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun parseArguments() {
        title = intent.getStringExtra(TITLE)!!
        urlPath = intent.getStringExtra(URL_TO_OPEN)!!
        isAuthenticationRequired = intent.getBooleanExtra(IS_AUTHENTICATION_REQUIRED,true)
    }

    private fun initializeWebViewFragment() {
        webViewFragment = WebViewFragment()
        webViewFragment.arguments = getWebViewArguments()
        webViewFragment.setListener(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, webViewFragment)
            .commit()
    }

    private fun getWebViewArguments(): Bundle {
        return Bundle().apply {
            this.putString(WebViewFragment.URL_TO_OPEN, urlPath)
            if (isAuthenticationRequired) {
                this.putBoolean(WebViewFragment.IS_AUTHENTICATION_REQUIRED, true)
                this.putBoolean(WebViewFragment.ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW, false)
            } else {
                this.putBoolean(WebViewFragment.IS_AUTHENTICATION_REQUIRED, false)
                this.putBoolean(WebViewFragment.ALLOW_NON_INSTITUTE_URL_IN_WEB_VIEW, true)
            }
        }
    }

    abstract override fun onWebViewInitializationSuccess()

    companion object {
        const val TITLE = "TITLE"
        const val URL_TO_OPEN = "URL"
        const val IS_AUTHENTICATION_REQUIRED = "IS_AUTHENTICATION_REQUIRED"

        fun createIntent(
            currentContext: Context,
            title: String,
            urlPath: String,
            isAuthenticationRequired: Boolean,
            activityToOpen: Class<out AbstractWebViewActivity>
        ): Intent {
            return Intent(currentContext, activityToOpen).apply {
                putExtra(TITLE, title)
                putExtra(URL_TO_OPEN, urlPath)
                putExtra(IS_AUTHENTICATION_REQUIRED, isAuthenticationRequired)
            }
        }
    }
}

class WebViewWithSSOActivity:AbstractWebViewActivity(){

    override fun onWebViewInitializationSuccess() {}

}
