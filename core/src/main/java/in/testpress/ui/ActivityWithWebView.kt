package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.core.TestpressException
import `in`.testpress.databinding.BaseTestpressContainerLayoutBinding
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.fragments.WebViewFragment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import java.io.IOException

class ActivityWithWebView : BaseToolBarActivity(), EmptyViewListener, WebViewFragment.Listener {

    private var _binding: BaseTestpressContainerLayoutBinding? = null
    private val binding: BaseTestpressContainerLayoutBinding get() = _binding!!
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var webViewFragment: WebViewFragment
    private lateinit var title: String
    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = BaseTestpressContainerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        url = intent.getStringExtra(URL_TO_OPEN)!!
    }

    private fun initializeEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, emptyViewFragment)
        transaction.commit()
    }

    private fun initializeWebViewFragment() {
        webViewFragment = WebViewFragment(
            url = url,
            webViewFragmentSettings = WebViewFragment.Settings(
                showLoadingBetweenPages = false,
                isSSORequired = false,
                allowNonInstituteUrlInWebView = true
            )
        )
        webViewFragment.setListener(this)
        replaceWebViewFragment()
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
        webViewFragment.retry()
    }

    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(MyJavaScriptInterface(this), "Android")
    }

    override fun onError(exception: TestpressException?) {
        Log.d("TAG", "onError: ")
        showErrorView(exception)
    }

    private fun replaceWebViewFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, webViewFragment)
            .commit()
    }

    companion object {
        const val TITLE = "TITLE"
        const val URL_TO_OPEN = "URL"

        @JvmStatic
        fun createUrlIntent(
            context: Context,
            title: String,
            url: String,
        ): Intent {
            return Intent(context, ActivityWithWebView::class.java).apply {
                putExtra(TITLE, title)
                putExtra(URL_TO_OPEN, url)
            }
        }
    }

}

open class BaseJavaScriptInterface(activity: Activity)

class MyJavaScriptInterface(val activity: Activity) : BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun onUrlChange(string: String) {
        Toast.makeText(activity, string, Toast.LENGTH_SHORT).show()
    }

}
