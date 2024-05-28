package `in`.testpress.course.util

import `in`.testpress.course.R
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
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride
import com.google.android.exoplayer2.ui.TrackSelectionView
import com.google.common.collect.ImmutableList

open class TrackSelectionDialog(
    private val tracks : Tracks,
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
    var overrides: MutableMap<TrackGroup, TrackSelectionOverride>
    var onClickListener: DialogInterface.OnClickListener? = null

    constructor(trackSelector: DefaultTrackSelector, tracks : Tracks) : this(
        tracks,
        trackSelector.parameters,
        trackSelector.currentMappedTrackInfo!!
    )

    init {
        //val selectionOverrides = parameters.getSelectionOverride(rendererIndex, trackGroup)
        overrides = parameters.overrides
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
        trackSelectionView = view.findViewById(R.id.exo_track_selection_view)
        trackSelectionView.setShowDisableOption(false)
        trackSelectionView.setAllowAdaptiveSelections(allowAdaptiveSelections)
        trackSelectionView.setAllowMultipleOverrides(false)
        trackSelectionView.setTrackNameProvider(ExoPlayerTrackNameProvider())
        var trackGroups = ArrayList<Tracks.Group>()
        for (trackType in SUPPORTED_TRACK_TYPES) {
            trackGroups = ArrayList<Tracks.Group>()
            for (trackGroup in tracks.groups) {
                if (trackGroup.type == trackType) {
                    trackGroups.add(trackGroup)
                }
            }
        }
        trackSelectionView.init(trackGroups, false, overrides, null, this)
    }

    val SUPPORTED_TRACK_TYPES: ImmutableList<Int> =
        ImmutableList.of(C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT)

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

//    override fun onTrackSelectionChanged(
//        isDisabled: Boolean,
//        overrides: MutableList<DefaultTrackSelector.SelectionOverride>
//    ) {
//        overrides.let {
//            this.overrides = it
//        }
//    }

    override fun onTrackSelectionChanged(
        isDisabled: Boolean,
        overrides: MutableMap<TrackGroup, TrackSelectionOverride>
    ) {
        overrides.let {
            this.overrides = it
        }
    }
}