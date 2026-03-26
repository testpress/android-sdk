package `in`.testpress.course.ui

import android.view.View

interface VideoTranscriptSidePanelInterface {
    fun showVideoTranscriptSidePanel(subtitleUrl: String)
    fun hideVideoTranscriptSidePanel(notifyHost: Boolean = true)
}

class VideoTranscriptSidePanelView(
    private val onSeek: (seconds: Double) -> Unit,
    private val onCloseRequested: () -> Unit,
    private val currentPositionSecondsProvider: () -> Float,
) {
    private var panelView: VideoTranscriptPanelView? = null
    private var rootView: View? = null

    fun createView(context: android.content.Context): View {
        if (rootView == null) {
            panelView = VideoTranscriptPanelView(
                onSeek = onSeek,
                onCloseRequested = onCloseRequested,
            )
            rootView = panelView!!.createView(context)
        }
        return rootView!!
    }

    fun mount(subtitleUrl: String) {
        val view = panelView ?: return
        view.currentPositionSecondsProvider = currentPositionSecondsProvider
        view.mount(subtitleUrl)
        view.startSync()
    }

    fun onHidden() {
        panelView?.stopSync()
    }

    fun destroy() {
        panelView?.destroy()
        panelView = null
        rootView = null
    }
}
