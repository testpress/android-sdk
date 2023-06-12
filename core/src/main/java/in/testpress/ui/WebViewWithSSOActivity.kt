package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.databinding.BaseTestpressWebviewContainerLayoutBinding
import `in`.testpress.fragments.WebViewFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Activity that extends this activity must override initializeWebViewFragmentListener() method
 * and implement WebViewFragment.Listener.
 */

open class WebViewWithSSOActivity : BaseToolBarActivity(), WebViewFragment.Listener {

    private var _layout: BaseTestpressWebviewContainerLayoutBinding? = null
    private val layout: BaseTestpressWebviewContainerLayoutBinding get() = _layout!!
    protected lateinit var webViewFragment: WebViewFragment
    private lateinit var title: String
    private lateinit var urlPath: String
    private var isSSORequired: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _layout = BaseTestpressWebviewContainerLayoutBinding.inflate(layoutInflater)
        setContentView(layout.root)
        parseArguments()
        setActionBarTitle(title)
        initializeWebViewFragment()
        initializeWebViewFragmentListener()
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
        isSSORequired = intent.getBooleanExtra(IS_SSO_REQUIRED,true)
    }

    protected open fun initializeWebViewFragment() {
        webViewFragment = WebViewFragment(
            url = urlPath,
            webViewFragmentSettings = getWebViewFragmentSettings()
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, webViewFragment)
            .commit()
    }

    protected open fun initializeWebViewFragmentListener() {
        webViewFragment.setListener(this)
    }

    private fun getWebViewFragmentSettings():WebViewFragment.Settings {
        return if (isSSORequired){
            WebViewFragment.Settings()
        } else {
            WebViewFragment.Settings(
                isSSORequired = false,
                allowNonInstituteUrlInWebView = true
            )
        }
    }

    override fun onWebViewInitializationSuccess() {

    }

    companion object {
        const val TITLE = "TITLE"
        const val URL_TO_OPEN = "URL"
        const val IS_SSO_REQUIRED = "IS_SSO_REQUIRED"

        fun createIntent(
            currentContext: Context,
            title: String,
            urlPath: String,
            isSSORequired: Boolean,
            klass: Class<out WebViewWithSSOActivity>
        ): Intent {
            return Intent(currentContext, klass).apply {
                putExtra(TITLE, title)
                putExtra(URL_TO_OPEN, urlPath)
                putExtra(IS_SSO_REQUIRED, isSSORequired)
            }
        }
    }
}
