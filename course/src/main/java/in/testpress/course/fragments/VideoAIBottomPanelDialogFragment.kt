package `in`.testpress.course.fragments

import `in`.testpress.course.R
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
 * Portrait-only bottom panel dialog for Video AI.
 * 
 * This is intentionally NOT a BottomSheetDialog because BottomSheet uses a full-screen 
 * dialog window, which blocks touches to the video player even outside the visible sheet area.
 */
class VideoAIBottomPanelDialogFragment : DialogFragment(), VideoAIFragment.Host {

    companion object {
        private const val DEFAULT_TAG = "VideoAI_BottomPanel"
        const val INNER_FRAGMENT_TAG = "VideoAI_Content"

        fun showOrReuse(host: androidx.fragment.app.Fragment, assetId: String, notesUrl: String?) {
            val fm = host.childFragmentManager
            val existing = fm.findFragmentByTag(DEFAULT_TAG) as? VideoAIBottomPanelDialogFragment
            
            if (existing != null) {
                existing.handleReuse(assetId, notesUrl)
            } else {
                createNewInstance(fm, assetId, notesUrl)
            }
        }

        private fun createNewInstance(fm: androidx.fragment.app.FragmentManager, assetId: String, notesUrl: String?) {
            VideoAIBottomPanelDialogFragment().apply {
                arguments = createArgs(assetId, notesUrl)
            }.show(fm, DEFAULT_TAG)
        }

        private fun createArgs(assetId: String, notesUrl: String?): Bundle = Bundle().apply {
            putString(VideoAIFragment.ARG_ASSET_ID, assetId)
            putString(VideoAIFragment.ARG_NOTES_URL, notesUrl ?: "")
        }

        fun dismissIfPresent(host: androidx.fragment.app.Fragment) {
            val fm = host.childFragmentManager
            (fm.findFragmentByTag(DEFAULT_TAG) as? VideoAIBottomPanelDialogFragment)
                ?.dismissAllowingStateLoss()
        }
    }

    private var assetId: String = ""
    private var notesUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        parseArguments()
    }

    private fun parseArguments() {
        assetId = arguments?.getString(VideoAIFragment.ARG_ASSET_ID).orEmpty()
        notesUrl = arguments?.getString(VideoAIFragment.ARG_NOTES_URL)
    }

    private fun handleReuse(newAssetId: String, newNotesUrl: String?) {
        updateArguments(newAssetId, newNotesUrl)
        if (dialog != null) {
            dialog?.show()
            refreshPanelAppearance()
            mountVideoAIContent()
        } else {
            // Re-show logic if fragment was restored without an active dialog
            dismissAllowingStateLoss()
            createNewInstance(parentFragmentManager, newAssetId, newNotesUrl)
        }
    }

    private fun updateArguments(newAssetId: String, newNotesUrl: String?) {
        arguments = createArgs(newAssetId, newNotesUrl)
        parseArguments()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply { 
            setCanceledOnTouchOutside(false) 
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.learnlens_video_ai_dialog_container, container, false)
    }

    override fun onStart() {
        super.onStart()
        if (resources.configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
            dismissAllowingStateLoss()
            return
        }
        refreshPanelAppearance()
        mountVideoAIContent()
    }

    override fun onResume() {
        super.onResume()
        refreshPanelAppearance()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT) {
            dismissAllowingStateLoss()
        }
    }

    private fun mountVideoAIContent() {
        if (assetId.isBlank()) {
            dismissAllowingStateLoss()
            return
        }

        val fm = childFragmentManager
        val existing = fm.findFragmentByTag(INNER_FRAGMENT_TAG) as? VideoAIFragment

        if (existing != null && existing.arguments?.getString(VideoAIFragment.ARG_ASSET_ID) == assetId) {
            return
        }

        val fragment = VideoAIFragment.newInstance(assetId, notesUrl)
        fm.beginTransaction()
            .replace(R.id.content_container, fragment, INNER_FRAGMENT_TAG)
            .commitNowAllowingStateLoss()
    }

    private fun refreshPanelAppearance() {
        val window = dialog?.window ?: return
        
        applyPanelConstraints(window)
        configureWindowFlags(window)
    }

    private fun applyPanelConstraints(window: android.view.Window) {
        val screenHeight = getScreenHeight()
        val minHeight = dpToPx(320)
        val maxHeight = screenHeight - dpToPx(120)
        
        val targetHeight = max((screenHeight * 0.6f).toInt(), minHeight).coerceAtMost(maxHeight)
        
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

    private fun resolveHost(): VideoAIFragment.Host? {
        return (parentFragment as? VideoAIFragment.Host) ?: (activity as? VideoAIFragment.Host)
    }

    override fun onVideoAISeek(seconds: Double) {
        resolveHost()?.onVideoAISeek(seconds)
    }

    override fun onVideoAICloseRequested() {
        dialog?.hide()
    }
}
