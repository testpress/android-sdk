package `in`.testpress.exam.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.SSOUrl
import `in`.testpress.network.TestpressApiClient
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.R

class MindsetInsightsActivity : AbstractWebViewActivity() {

    private var isSsoUrlFetched = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        fetchSsoLink()
    }

    override fun initializeWebViewFragment() {
        if (!isSsoUrlFetched) return
        webViewFragment = WebViewFragment()      
        webViewFragment.arguments = android.os.Bundle().apply {
            putString(WebViewFragment.URL_TO_OPEN, urlPath)
            putBoolean(WebViewFragment.IS_AUTHENTICATION_REQUIRED, true)
            putInt(WebViewFragment.CACHE_MODE, WebSettings.LOAD_NO_CACHE)
        }
        webViewFragment.setListener(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, webViewFragment)
            .commitAllowingStateLoss()
    }

    private fun fetchSsoLink() {
        val session = TestpressSdk.getTestpressSession(this)
        if (session == null) {
            isSsoUrlFetched = true
            initializeWebViewFragment()
            return
        }

        TestpressApiClient(this, session).ssourl.enqueue(object : TestpressCallback<SSOUrl>() {
            override fun onSuccess(result: SSOUrl?) {
                val ssoUrl = result?.ssoUrl
                if (ssoUrl.isNullOrBlank()) {
                    isSsoUrlFetched = true
                    initializeWebViewFragment()
                    return
                }
                val nextUrl = Uri.encode(urlPath)
                val separator = if (ssoUrl.contains("?")) "&" else "?"
                val cleanBaseUrl = session.instituteSettings.baseUrl.trimEnd('/')
                val cleanSsoUrl = if (ssoUrl.startsWith("/")) ssoUrl else "/$ssoUrl"
                urlPath = "$cleanBaseUrl$cleanSsoUrl${separator}next=$nextUrl"
                isSsoUrlFetched = true
                initializeWebViewFragment()
            }

            override fun onException(exception: TestpressException?) {
                isSsoUrlFetched = true
                initializeWebViewFragment()
            }
        })
    }

    override fun onWebViewInitializationSuccess() {
        // No additional setup needed for this read-only insights page.
    }

    companion object {
        @JvmStatic
        fun createIntent(
            context: Context,
            baseUrl: String,
            userExamId: Long
        ): Intent {
            val cleanBaseUrl = baseUrl.trimEnd('/')
            val url = "$cleanBaseUrl/exams/review/$userExamId/mindset_insights/"
            return AbstractWebViewActivity.createIntent(
                context,
                title = "",
                urlPath = url,
                isAuthenticationRequired = true,
                activityToOpen = MindsetInsightsActivity::class.java
            )
        }
    }
}
