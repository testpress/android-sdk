package `in`.testpress.course.ui.videoconference.webview

import android.webkit.WebView
import `in`.testpress.course.domain.VideoConferenceProviderType

interface WebViewConferenceProviderHandler {
    fun attach(webView: WebView) {}

    fun onPageStarted(url: String?) {}

    fun onPageFinished(url: String?)

    companion object {
        fun create(
            providerType: VideoConferenceProviderType,
            showLoader: () -> Unit,
            hideLoader: () -> Unit
        ): WebViewConferenceProviderHandler? {
            return when (providerType) {
                VideoConferenceProviderType.MICROSOFT_TEAMS ->
                    TeamsWebViewConferenceProviderHandler(showLoader, hideLoader)
                else -> null
            }
        }
    }
}
