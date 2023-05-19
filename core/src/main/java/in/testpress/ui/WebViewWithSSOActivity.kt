package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.core.TestpressException
import `in`.testpress.databinding.BaseTestpressWebviewContainerLayoutBinding
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.fragments.WebViewFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import java.io.IOException

class WebViewWithSSOActivity : BaseToolBarActivity(), EmptyViewListener, WebViewFragment.Listener {

    private var _layout: BaseTestpressWebviewContainerLayoutBinding? = null
    private val layout: BaseTestpressWebviewContainerLayoutBinding get() = _layout!!
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var webViewFragment: WebViewFragment
    private lateinit var title: String
    private lateinit var urlPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _layout = BaseTestpressWebviewContainerLayoutBinding.inflate(layoutInflater)
        setContentView(layout.root)
        parseArguments()
        setActionBarTitle(title)
        initializeEmptyViewFragment()
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
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }

    private fun initializeWebViewFragment() {
        webViewFragment = WebViewFragment(
            url = urlPath,
            webViewFragmentSettings = WebViewFragment.Settings(
                showLoadingBetweenPages = false,
                isSSORequired = true,
                allowNonInstituteUrlInWebView = false
            )
        )
        webViewFragment.setListener(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, webViewFragment)
            .commit()
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
        hideEmptyViewShowWebView()
        webViewFragment.retryLoad()
    }

    override fun onWebViewInitializationSuccess() {}

    override fun onError(exception: TestpressException?) {
        hideWebViewShowEmptyView()
        showErrorView(exception)
    }

    private fun hideEmptyViewShowWebView() {
        layout.emptyViewContainer.isVisible = false
        layout.fragmentContainer.isVisible = true
    }

    private fun hideWebViewShowEmptyView() {
        layout.emptyViewContainer.isVisible = false
        layout.fragmentContainer.isVisible = true
    }

    companion object {
        const val TITLE = "TITLE"
        const val URL_TO_OPEN = "URL"

        @JvmStatic
        fun createUrlIntent(
            context: Context,
            title: String,
            urlPath: String,
        ): Intent {
            return Intent(context, WebViewWithSSOActivity::class.java).apply {
                putExtra(TITLE, title)
                putExtra(URL_TO_OPEN, urlPath)
            }
        }
    }

}
