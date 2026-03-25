package `in`.testpress.course.util

import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressException
import `in`.testpress.course.R
import `in`.testpress.course.util.VideoAIUserIdProvider
import `in`.testpress.util.webview.BaseWebViewClient
import `in`.testpress.util.webview.WebView
import `in`.testpress.util.webview.WebViewEventListener
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class VideoAIPlayerController(
    private val activity: Activity,
    private val onSeek: (seconds: Double) -> Unit,
    private val onCloseRequested: () -> Unit,
) : WebViewEventListener, VideoAIJsInterface.Host {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var rootView: View? = null
    private var progressBar: ProgressBar? = null
    private var webViewContainer: FrameLayout? = null
    private var webView: WebView? = null

    private var mountedAssetId: String? = null
    private var mountedNotesUrl: String? = null

    fun createView(context: android.content.Context): View {
        if (rootView == null) {
            rootView = LayoutInflater.from(context).inflate(R.layout.learnlens_video_ai_sheet, null, false)
            progressBar = rootView?.findViewById(R.id.pb_loading)
            webViewContainer = rootView?.findViewById(R.id.webview_container)
        }
        return rootView!!
    }

    fun mount(assetId: String, notesUrl: String?) {
        val root = rootView ?: return
        
        if (webView == null) {
            val wv = WebView(activity)
            webView = wv
            webViewContainer?.addView(wv)
            configureWebView(wv)
        }

        if (mountedAssetId != assetId || mountedNotesUrl != notesUrl) {
            mountedAssetId = assetId
            mountedNotesUrl = notesUrl
            load(assetId, notesUrl.orEmpty())
        }
    }

    fun destroy() {
        scope.cancel()
        webView?.let { wv ->
            webViewContainer?.removeView(wv)
            wv.destroy()
        }
        webView = null
        webViewContainer = null
        progressBar = null
        (rootView?.parent as? FrameLayout)?.removeView(rootView)
        rootView = null
        mountedAssetId = null
        mountedNotesUrl = null
    }

    private fun configureWebView(wv: WebView) {
        wv.enableFileAccess()
        wv.webViewClient = BaseWebViewClient(this)
        wv.addJavascriptInterface(VideoAIJsInterface(activity, this), "AndroidVideoAI")
    }

    private fun load(assetId: String, notesUrl: String) {
        showLoading()
        val authToken = TestpressSdk.getTestpressSession(activity)?.token ?: ""
        val cacheDir = File(activity.filesDir, "web_assets")

        scope.launch {
            val userId = VideoAIUserIdProvider.getUserId(activity)
            val replacements = mapOf(
                "ASSET_ID" to assetId,
                "AUTH_TOKEN" to authToken,
                "USER_ID" to userId,
                "NOTES_URL" to notesUrl
            )
            webView?.loadTemplateAndCacheResources(
                "video_ai_template.html",
                replacements,
                "file://${cacheDir.absolutePath}/"
            )
        }
    }

    private fun showLoading() {
        progressBar?.isVisible = true
        webViewContainer?.isVisible = false
    }

    private fun hideLoading() {
        progressBar?.isVisible = false
        webViewContainer?.isVisible = true
    }

    override fun onLoadingStarted() = showLoading()
    override fun onLoadingFinished() = hideLoading()
    override fun onError(exception: TestpressException) {
        hideLoading()
    }
    override fun isViewActive(): Boolean = true

    override fun onSeek(seconds: Double) = onSeek.invoke(seconds)
    override fun onRequestClose() = onCloseRequested.invoke()
}
