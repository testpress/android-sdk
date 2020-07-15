package `in`.testpress.course.util

import `in`.testpress.course.R
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.TrackSelectionView
import com.google.android.exoplayer2.util.Assertions
import kotlinx.android.synthetic.main.track_selection_dialog.*

class TrackSelectionDialog(private val trackSelector: DefaultTrackSelector) : DialogFragment(),
    TrackSelectionView.TrackSelectionListener {

    private lateinit var trackSelectionView: TrackSelectionView
    private var allowAdaptiveSelections = false
    private val parameters = trackSelector.parameters
    private val mappedTrackInfo = Assertions.checkNotNull(trackSelector.currentMappedTrackInfo)
    private val rendererIndex = getRendererIndex(C.TRACK_TYPE_VIDEO)
    private val trackGroup = mappedTrackInfo.getTrackGroups(rendererIndex)
    private var overrides: List<DefaultTrackSelector.SelectionOverride>

    init {
        val selectionOverrides = parameters.getSelectionOverride(rendererIndex, trackGroup)
        overrides = selectionOverrides?.let { listOf(it) } ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.track_selection_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTrackSelectionView(view)
        setOnClickListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AppCompatDialog(activity, R.style.TestpressAppCompatAlertDialogStyle)
        dialog.setTitle(R.string.quality_selector)
        return dialog
    }

    private fun initializeTrackSelectionView(view: View) {
        trackSelectionView = view.findViewById(R.id.exo_track_selection_view)
        trackSelectionView.setShowDisableOption(false)
        trackSelectionView.setAllowAdaptiveSelections(allowAdaptiveSelections)
        trackSelectionView.setAllowMultipleOverrides(false)
        trackSelectionView.init(mappedTrackInfo, rendererIndex, false, overrides, this)
    }

    private fun getRendererIndex(trackType: Int): Int {
        for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
            if (mappedTrackInfo.getRendererType(rendererIndex) == trackType) {
                return rendererIndex
            }
        }

        return -1
    }

    private fun setOnClickListeners() {
        okButton.setOnClickListener {
            changeSelectedTrack()
            dismiss()
        }

        cancelButton.setOnClickListener { dismiss() }
    }

    private fun changeSelectedTrack() {
        val parametersBuilder = parameters.buildUpon()
        parametersBuilder
            .clearSelectionOverrides(rendererIndex)
            .setSelectionOverride(
                rendererIndex,
                mappedTrackInfo.getTrackGroups(rendererIndex),
                overrides[0]
            )
        trackSelector.setParameters(parametersBuilder)
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