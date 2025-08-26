package `in`.testpress.course.fragments

import android.webkit.WebSettings
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.domain.DomainContent
import com.google.gson.JsonObject

class DomainRestrictedVideoFragment: WebViewVideoFragment() {
    private val session by lazy { TestpressSdk.getTestpressSession(activity!!) }

    override fun loadVideo(content: DomainContent) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(TestpressCourseApiClient.EMBED_CODE, content.video!!.embedCode)
        val url = "${session!!.instituteSettings.baseUrl}/${TestpressCourseApiClient.EMBED_DOMAIN_RESTRICTED_VIDEO_PATH}"
        webViewUtils.initWebViewAndPostUrl(url, jsonObject.toString(), activity)
        disableWebViewCache()
        webView.webChromeClient = fullScreenChromeClient
    }

    private fun disableWebViewCache() {
        webView.settings.apply {
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = false
        }
        webView.clearCache(true)
        webView.clearHistory()
    }
}