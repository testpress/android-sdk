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
    private var transcriptView: VideoTranscriptView? = null
    private var rootView: View? = null

    fun createView(context: android.content.Context): View {
        if (rootView == null) {
            transcriptView = VideoTranscriptView(
                onSeek = onSeek,
                onCloseRequested = onCloseRequested,
            )
            rootView = transcriptView!!.createView(context)
        }
        return rootView!!
    }

    fun mount(subtitleUrl: String) {
        val view = transcriptView ?: return
        view.currentPositionSecondsProvider = currentPositionSecondsProvider
        view.mount(subtitleUrl)
        view.startSync()
    }

    fun onHidden() {
        transcriptView?.stopSync()
    }

    fun destroy() {
        transcriptView?.destroy()
        transcriptView = null
        rootView = null
    }
}
