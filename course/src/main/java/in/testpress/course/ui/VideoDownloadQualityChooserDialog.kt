package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.databinding.LayoutDocumentViewerBinding
import `in`.testpress.course.databinding.VideoDownloadDialogBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.helpers.VideoDownloadRequestCreationHandler
import `in`.testpress.course.util.ExoPlayerTrackNameProvider
import `in`.testpress.course.util.TrackSelectionDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.ui.TrackSelectionView
import java.io.IOException

typealias OnSubmitListener = (DownloadRequest) -> Unit

class VideoDownloadQualityChooserDialog(val content: DomainContent) : DialogFragment(),
    TrackSelectionView.TrackSelectionListener,
    VideoDownloadRequestCreationHandler.Listener {
    private var _binding: VideoDownloadDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var trackSelectionView: TrackSelectionView
    lateinit var overrides: MutableMap<TrackGroup, TrackSelectionOverride>
    private var onSubmitListener: OnSubmitListener? = null
    lateinit var videoDownloadRequestCreateHandler: VideoDownloadRequestCreationHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoDownloadRequestCreateHandler =
            VideoDownloadRequestCreationHandler(
                requireContext(),
                content
            )
        videoDownloadRequestCreateHandler.listener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = VideoDownloadDialogBinding.inflate(inflater, container, false)
        return binding.root
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
        trackSelectionView.setAllowAdaptiveSelections(false)
        trackSelectionView.setAllowMultipleOverrides(false)
        trackSelectionView.visibility = View.GONE
        trackSelectionView.setTrackNameProvider(ExoPlayerTrackNameProvider())
    }

    private fun setOnClickListeners() {
        binding.okButton.setOnClickListener {
            if (::overrides.isInitialized) {
                val downloadRequest = videoDownloadRequestCreateHandler.buildDownloadRequest(overrides)
                onSubmitListener?.invoke(downloadRequest)
            }
            dismiss()
        }

        binding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun showLoading() {
        binding.loadingProgress.visibility = View.VISIBLE
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AppCompatDialog(requireContext(), android.R.style.Theme_Material_Light_Dialog_Alert)
        dialog.setTitle("Choose Download Quality")
        return dialog
    }

    override fun onDownloadRequestHandlerPrepared(
        mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
        tracks: Tracks?,
        rendererIndex: Int,
        overrides: MutableMap<TrackGroup, TrackSelectionOverride>
    ) {
        hideLoading()
        trackSelectionView.visibility = View.VISIBLE
        this.overrides = overrides
        val l = mutableListOf<Tracks.Group>()
        tracks?.groups?.asList()?.let { l.addAll(it) }
        trackSelectionView.init(l,false, overrides, null, this)

    }

    override fun onDownloadRequestHandlerPrepareError(helper: DownloadHelper, e: IOException) {
        hideLoading()
        binding.errorText.visibility = View.VISIBLE
        dialog?.setTitle(null)
        binding.cancelButton.visibility = View.GONE
        binding.okButton.setOnClickListener {
            dismiss()
        }
        Log.d("VideoDownload", "onDownloadRequestHandlerPrepareError: ${e.localizedMessage}")
    }

    private fun hideLoading() {
        binding.loadingProgress.visibility = View.GONE
    }

//    override fun onTrackSelectionChanged(
//        isDisabled: Boolean,
//        selectedOverrides: MutableList<DefaultTrackSelector.SelectionOverride>
//    ) {
//        this.overrides = selectedOverrides
//    }

    fun setOnSubmitListener(listener: OnSubmitListener) {
        onSubmitListener = listener
    }

    override fun onTrackSelectionChanged(
        isDisabled: Boolean,
        overrides: MutableMap<TrackGroup, TrackSelectionOverride>
    ) {
        this.overrides = overrides
    }
}