package `in`.testpress.exam.ui

import `in`.testpress.exam.R
import `in`.testpress.ui.BaseToolBarActivity
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebViewClient

private const val EXTERNAL_REVIEW_URL = "externalReviewUrl"


class AdvanceAnalyticsActivity : BaseToolBarActivity() {

    lateinit var webView: android.webkit.WebView

    companion object {
        fun createIntent(activity: Activity, externalReviewUrl: String): Intent {
            val intent = Intent(activity, AdvanceAnalyticsActivity::class.java)
            intent.putExtra(EXTERNAL_REVIEW_URL, externalReviewUrl)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_activity_advance_analytics)

        val externalReviewUrl = intent.getStringExtra(EXTERNAL_REVIEW_URL).toString()

        webView = findViewById(R.id.external_url_web_view)

        if (externalReviewUrl != null && externalReviewUrl != "") {
            webView.loadUrl(externalReviewUrl)
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = WebViewClient()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }


}