package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.ui.VideoTranscriptView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

interface VideoTranscriptHost {
    fun onVideoTranscriptSeek(seconds: Double)
    fun onVideoTranscriptCloseRequested()
    fun getVideoTranscriptCurrentPositionSeconds(): Float
}

/**
 * Inline transcript panel used in portrait, anchored below the video player.
 *
 * This avoids window-sized dialogs and keeps the panel consistently placed across devices.
 */
class VideoTranscriptFragment : Fragment() {

    companion object {
        const val ARG_SUBTITLE_URL = "arg_subtitle_url"

        fun newInstance(subtitleUrl: String): VideoTranscriptFragment {
            return VideoTranscriptFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SUBTITLE_URL, subtitleUrl)
                }
            }
        }
    }

    private var subtitleUrl: String = ""
    private var transcriptView: VideoTranscriptView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subtitleUrl = arguments?.getString(ARG_SUBTITLE_URL).orEmpty()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.video_transcript_dialog_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mountTranscriptContent()
    }

    override fun onStart() {
        super.onStart()
        transcriptView?.startSync()
    }

    override fun onStop() {
        transcriptView?.stopSync()
        super.onStop()
    }

    private fun mountTranscriptContent() {
        val container = view?.findViewById<ViewGroup>(R.id.content_container) ?: return
        if (transcriptView == null) {
            transcriptView = VideoTranscriptView(
                onSeek = { seconds -> resolveHost()?.onVideoTranscriptSeek(seconds) },
                onCloseRequested = { resolveHost()?.onVideoTranscriptCloseRequested() },
            )
            container.addView(transcriptView!!.createView(requireContext()))
        }

        transcriptView?.currentPositionSecondsProvider = { resolveHost()?.getVideoTranscriptCurrentPositionSeconds() ?: 0f }
        transcriptView?.mount(subtitleUrl)
    }

    private fun resolveHost(): VideoTranscriptHost? {
        return (parentFragment as? VideoTranscriptHost) ?: (activity as? VideoTranscriptHost)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        transcriptView?.destroy()
        transcriptView = null
    }
}
