package `in`.testpress.course.util

import `in`.testpress.course.R
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

class VideoAISidePanelContent(
    private val activity: Activity,
    private val onSeek: (seconds: Double) -> Unit,
    private val onCloseRequested: () -> Unit,
) : VideoAIJsInterface.Host {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var rootView: View? = null
    private var progressBar: ProgressBar? = null
    private var webViewContainer: FrameLayout? = null
    private var webViewRenderer: VideoAIWebViewRenderer? = null

    private var mountedAssetId: String? = null
    private var mountedNotesUrl: String? = null

    fun createView(context: android.content.Context): View {
        if (rootView == null) {
            rootView = LayoutInflater.from(context).inflate(R.layout.learnlens_video_ai_sheet, null, false)
            progressBar = rootView?.findViewById(R.id.pb_loading)
            webViewContainer = rootView?.findViewById(R.id.webview_container)

            val container = webViewContainer
            if (container != null) {
                webViewRenderer = VideoAIWebViewRenderer(
                    activity = activity,
                    scope = scope,
                    jsHost = this,
                    isViewActive = { true },
                    onLoadingChanged = { isLoading -> setLoading(isLoading) },
                    webChromeClient = null,
                ).also { it.attach(container) }
            }
        }
        return rootView!!
    }

    fun mount(assetId: String, notesUrl: String?) {
        if (rootView == null) return

        webViewRenderer?.let { renderer ->
            val container = webViewContainer
            if (container != null) renderer.attach(container)
        }

        if (mountedAssetId != assetId || mountedNotesUrl != notesUrl) {
            mountedAssetId = assetId
            mountedNotesUrl = notesUrl
            webViewRenderer?.mount(assetId, notesUrl)
        }
    }

    fun destroy() {
        scope.cancel()
        webViewRenderer?.destroy()
        webViewRenderer = null
        webViewContainer = null
        progressBar = null
        (rootView?.parent as? FrameLayout)?.removeView(rootView)
        rootView = null
        mountedAssetId = null
        mountedNotesUrl = null
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar?.isVisible = isLoading
        webViewContainer?.isVisible = !isLoading
    }

    override fun onSeek(seconds: Double) = onSeek.invoke(seconds)
    override fun onRequestClose() = onCloseRequested.invoke()
}
