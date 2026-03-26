package `in`.testpress.course.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressUserIdProvider
import `in`.testpress.course.util.VideoAIJsInterface
import `in`.testpress.util.webview.BaseWebChromeClient
import `in`.testpress.util.webview.BaseWebViewClient
import `in`.testpress.util.webview.WebView
import `in`.testpress.util.webview.WebViewEventListener
import android.annotation.SuppressLint
import android.app.Activity
import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

class VideoAIWebView(
    private val activity: Activity,
    private val scope: CoroutineScope,
    private val jsHost: VideoAIJsInterface.Host,
    private val isViewActive: () -> Boolean,
    private val onLoadingChanged: (isLoading: Boolean) -> Unit,
    private val webChromeClient: BaseWebChromeClient? = null,
) : WebViewEventListener {

    companion object {
        private const val TEMPLATE_NAME = "video_ai_template.html"
        private const val JS_INTERFACE_NAME = "AndroidVideoAI"
    }

    private var container: FrameLayout? = null
    private var webView: WebView? = null

    private var mountedAssetId: String? = null
    private var mountedNotesUrl: String? = null

    fun attach(container: FrameLayout) {
        this.container = container
        ensureWebView()
    }

    fun mount(assetId: String, notesUrl: String?) {
        if (assetId.isBlank()) return
        ensureWebView()

        val normalizedNotesUrl = notesUrl ?: ""
        if (mountedAssetId == assetId && mountedNotesUrl == normalizedNotesUrl) return

        mountedAssetId = assetId
        mountedNotesUrl = normalizedNotesUrl
        loadTemplate(assetId, normalizedNotesUrl)
    }

    fun destroy() {
        webView?.let { wv ->
            wv.stopLoading()
            container?.removeView(wv)
            wv.destroy()
        }
        webView = null
        container = null
        mountedAssetId = null
        mountedNotesUrl = null
    }

    @SuppressLint("JavascriptInterface", "AddJavascriptInterface")
    private fun ensureWebView() {
        val targetContainer = container ?: return
        if (webView != null) return

        val wv = WebView(activity)
        webView = wv
        targetContainer.addView(wv)

        wv.enableFileAccess()
        wv.webViewClient = BaseWebViewClient(this)
        webChromeClient?.let { wv.webChromeClient = it }
        wv.addJavascriptInterface(VideoAIJsInterface(activity, jsHost), JS_INTERFACE_NAME)
    }

    private fun loadTemplate(assetId: String, notesUrl: String) {
        val wv = webView ?: return
        onLoadingChanged(true)

        val authToken = TestpressSdk.getTestpressSession(activity)?.token ?: ""
        val cacheDir = File(activity.filesDir, "web_assets")

        scope.launch {
            if (!isViewActive()) return@launch

            val userId = TestpressUserIdProvider.getUserId(activity)
            val replacements = mapOf(
                "ASSET_ID" to assetId,
                "AUTH_TOKEN" to authToken,
                "USER_ID" to userId,
                "NOTES_URL" to notesUrl
            )

            wv.loadTemplateAndCacheResources(
                TEMPLATE_NAME,
                replacements,
                "file://${cacheDir.absolutePath}/"
            )
        }
    }

    override fun onLoadingStarted() = onLoadingChanged(true)
    override fun onLoadingFinished() = onLoadingChanged(false)
    override fun onError(exception: TestpressException) = onLoadingChanged(false)
    override fun isViewActive(): Boolean = isViewActive.invoke()
}
