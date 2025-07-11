package `in`.testpress.course.util


import `in`.testpress.course.databinding.LayoutDocumentViewerBinding
import `in`.testpress.course.databinding.TrackSelectionDialogBinding
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.TrackSelectionView
import `in`.testpress.course.R

open class TrackSelectionDialog(
    private val parameters: DefaultTrackSelector.Parameters,
    open val mappedTrackInfo: MappingTrackSelector.MappedTrackInfo
) : DialogFragment(),
    TrackSelectionView.TrackSelectionListener {
    private var _binding: TrackSelectionDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var trackSelectionView: TrackSelectionView
    private var allowAdaptiveSelections = false
    private val rendererIndex = ExoPlayerUtil.getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo)
    private val trackGroup = mappedTrackInfo.getTrackGroups(rendererIndex)
    var overrides: List<DefaultTrackSelector.SelectionOverride>
    var onClickListener: DialogInterface.OnClickListener? = null

    constructor(trackSelector: DefaultTrackSelector) : this(
        trackSelector.parameters,
        trackSelector.currentMappedTrackInfo!!
    )

    init {
        val selectionOverrides = parameters.getSelectionOverride(rendererIndex, trackGroup)
        overrides = selectionOverrides?.let { listOf(it) } ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TrackSelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTrackSelectionView(view)
        setOnClickListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AppCompatDialog(requireContext(), android.R.style.Theme_Material_Light_Dialog_Alert)
        dialog.setTitle(R.string.quality_selector)
        return dialog
    }

    private fun initializeTrackSelectionView(view: View) {
        trackSelectionView = view.findViewById(com.google.android.exoplayer2.ui.R.id.exo_track_selection_view)
        trackSelectionView.setShowDisableOption(false)
        trackSelectionView.setAllowAdaptiveSelections(allowAdaptiveSelections)
        trackSelectionView.setAllowMultipleOverrides(false)
        trackSelectionView.setTrackNameProvider(ExoPlayerTrackNameProvider())
        trackSelectionView.init(mappedTrackInfo, rendererIndex, false, overrides, null, this)
    }

    private fun setOnClickListeners() {
        binding.okButton.setOnClickListener {
            onClickListener?.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }

        binding.cancelButton.setOnClickListener { dismiss() }
    }

    fun setAllowAdaptiveSelections(allow: Boolean) {
        allowAdaptiveSelections = allow
    }

    override fun onTrackSelectionChanged(
        isDisabled: Boolean,
        overrides: MutableList<DefaultTrackSelector.SelectionOverride>
    ) {
        overrides.let {
            this.overrides = it
        }
    }
}