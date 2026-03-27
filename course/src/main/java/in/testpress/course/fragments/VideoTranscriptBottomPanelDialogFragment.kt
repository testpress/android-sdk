package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.ui.VideoTranscriptView
import android.app.Dialog
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import kotlin.math.max

/**
 * Portrait-only bottom panel dialog for Transcript.
 *
 * This is intentionally NOT a BottomSheetDialog because BottomSheet uses a full-screen
 * dialog window, which blocks touches to the video player even outside the visible sheet area.
 */
class VideoTranscriptBottomPanelDialogFragment : DialogFragment() {

    interface Host {
        fun onVideoTranscriptSeek(seconds: Double)
        fun onVideoTranscriptCloseRequested()
        fun getVideoTranscriptCurrentPositionSeconds(): Float
    }

    companion object {
        private const val DEFAULT_TAG = "VideoTranscript_BottomPanel"
        private const val ARG_SUBTITLE_URL = "arg_subtitle_url"

        fun showOrReuse(host: androidx.fragment.app.Fragment, subtitleUrl: String) {
            val fm = host.childFragmentManager
            val existing = fm.findFragmentByTag(DEFAULT_TAG) as? VideoTranscriptBottomPanelDialogFragment

            if (existing != null) {
                existing.handleReuse(subtitleUrl)
            } else {
                createNewInstance(fm, subtitleUrl)
            }
        }

        private fun createNewInstance(fm: androidx.fragment.app.FragmentManager, subtitleUrl: String) {
            VideoTranscriptBottomPanelDialogFragment().apply {
                arguments = createArgs(subtitleUrl)
            }.show(fm, DEFAULT_TAG)
        }

        private fun createArgs(subtitleUrl: String): Bundle = Bundle().apply {
            putString(ARG_SUBTITLE_URL, subtitleUrl)
        }

        fun dismissIfPresent(host: androidx.fragment.app.Fragment) {
            val fm = host.childFragmentManager
            (fm.findFragmentByTag(DEFAULT_TAG) as? VideoTranscriptBottomPanelDialogFragment)
                ?.dismissAllowingStateLoss()
        }

        fun hideIfPresent(host: androidx.fragment.app.Fragment) {
            val fm = host.childFragmentManager
            (fm.findFragmentByTag(DEFAULT_TAG) as? VideoTranscriptBottomPanelDialogFragment)
                ?.dialog
                ?.hide()
        }
    }

    private var subtitleUrl: String = ""
    private var transcriptView: VideoTranscriptView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        parseArguments()
    }

    private fun parseArguments() {
        subtitleUrl = arguments?.getString(ARG_SUBTITLE_URL).orEmpty()
    }

    private fun handleReuse(newSubtitleUrl: String) {
        arguments = createArgs(newSubtitleUrl)
        parseArguments()
        dialog?.show()
        refreshPanelAppearance()
        mountTranscriptContent()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.video_transcript_dialog_container, container, false)
    }

    override fun onStart() {
        super.onStart()
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            dialog?.hide()
            return
        }
        refreshPanelAppearance()
        mountTranscriptContent()
        transcriptView?.startSync()
    }

    override fun onStop() {
        transcriptView?.stopSync()
        super.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT) {
            dialog?.hide()
        }
    }

    private fun mountTranscriptContent() {
        val container = view?.findViewById<ViewGroup>(R.id.content_container) ?: return
        if (transcriptView == null) {
            transcriptView = VideoTranscriptView(
                onSeek = { seconds -> resolveHost()?.onVideoTranscriptSeek(seconds) },
                onCloseRequested = {
                    dialog?.hide()
                    resolveHost()?.onVideoTranscriptCloseRequested()
                },
            )
            container.addView(transcriptView!!.createView(requireContext()))
        }

        transcriptView?.currentPositionSecondsProvider = { resolveHost()?.getVideoTranscriptCurrentPositionSeconds() ?: 0f }
        transcriptView?.mount(subtitleUrl)
    }

    private fun refreshPanelAppearance() {
        val window = dialog?.window ?: return
        applyPanelConstraints(window)
        configureWindowFlags(window)
    }

    private fun applyPanelConstraints(window: android.view.Window) {
        val screenHeight = getScreenHeight()
        val isTablet = resources.configuration.smallestScreenWidthDp >= 600

        val targetHeight = if (isTablet) {
            val minHeight = dpToPx(280)
            val preferredHeight = (screenHeight * 0.48f).toInt()
            val maxHeight = (screenHeight * 0.55f).toInt().coerceAtLeast(minHeight)
            preferredHeight.coerceIn(minHeight, maxHeight)
        } else {
            val minHeight = dpToPx(320)
            val maxHeight = screenHeight - dpToPx(120)
            max((screenHeight * 0.6f).toInt(), minHeight).coerceAtMost(maxHeight)
        }

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, targetHeight)
        window.setGravity(Gravity.BOTTOM)
    }

    private fun configureWindowFlags(window: android.view.Window) {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            setBackgroundDrawableResource(android.R.color.transparent)
            decorView.setPadding(0, 0, 0, 0)
        }
    }

    private fun getScreenHeight(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = requireActivity().windowManager.currentWindowMetrics
            windowMetrics.bounds.height()
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
            metrics.heightPixels
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun resolveHost(): Host? {
        return (parentFragment as? Host) ?: (activity as? Host)
    }
}
