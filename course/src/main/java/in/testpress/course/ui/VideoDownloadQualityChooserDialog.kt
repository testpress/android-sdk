package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.helpers.VideoDownloadRequestCreationHandler
import `in`.testpress.course.util.ExoPlayerTrackNameProvider
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.TrackSelectionView
import kotlinx.android.synthetic.main.video_download_dialog.*
import java.io.IOException

typealias OnSubmitListener = (DownloadRequest) -> Unit

class VideoDownloadQualityChooserDialog(val content: DomainContent) : DialogFragment(),
    TrackSelectionView.TrackSelectionListener,
    VideoDownloadRequestCreationHandler.Listener {
    private lateinit var trackSelectionView: TrackSelectionView
    lateinit var overrides: List<DefaultTrackSelector.SelectionOverride>
    private var onSubmitListener: OnSubmitListener? = null
    lateinit var videoDownloadRequestCreateHandler: VideoDownloadRequestCreationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoDownloadRequestCreateHandler =
            VideoDownloadRequestCreationHandler(
                requireContext(),
                content.video!!.hlsUrl()!!, content.title!!
            )
        videoDownloadRequestCreateHandler.listener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_download_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        initializeTrackSelectionView(view)
        showLoading()
    }
    
    private fun initializeTrackSelectionView(view: View) {
        trackSelectionView = view.findViewById(R.id.exo_track_selection_view)
        trackSelectionView.setShowDisableOption(false)
        trackSelectionView.setAllowAdaptiveSelections(true)
        trackSelectionView.setAllowMultipleOverrides(false)
        trackSelectionView.visibility = View.GONE
        trackSelectionView.setTrackNameProvider(ExoPlayerTrackNameProvider())
    }

    private fun setOnClickListeners() {
        okButton.setOnClickListener {
            val downloadRequest = videoDownloadRequestCreateHandler.buildDownloadRequest(overrides)
            onSubmitListener?.invoke(downloadRequest)
            dismiss()
        }

        cancelButton.setOnClickListener { dismiss() }
    }

    private fun showLoading() {
        loadingProgress.visibility = View.VISIBLE
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AppCompatDialog(activity, R.style.TestpressAppCompatAlertDialogStyle)
        dialog.setTitle("Choose Download Quality")
        return dialog
    }

    override fun onDownloadRequestHandlerPrepared(
        mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
        rendererIndex: Int,
        overrides: List<DefaultTrackSelector.SelectionOverride>
    ) {
        hideLoading()
        trackSelectionView.visibility = View.VISIBLE
        this.overrides = overrides
        trackSelectionView.init(mappedTrackInfo, rendererIndex, false, overrides, this)
    }

    override fun onDownloadRequestHandlerPrepareError(helper: DownloadHelper, e: IOException) {
        hideLoading()
        errorText.visibility = View.VISIBLE
        dialog?.setTitle(null)
        cancelButton.visibility = View.GONE
        okButton.setOnClickListener {
            dismiss()
        }
        Log.d("VideoDownload", "onDownloadRequestHandlerPrepareError: ${e.localizedMessage}")
    }

    private fun hideLoading() {
        loadingProgress.visibility = View.GONE
    }

    override fun onTrackSelectionChanged(
        isDisabled: Boolean,
        selectedOverrides: MutableList<DefaultTrackSelector.SelectionOverride>
    ) {
        this.overrides = selectedOverrides
    }

    fun setOnSubmitListener(listener: OnSubmitListener) {
        onSubmitListener = listener
    }
}