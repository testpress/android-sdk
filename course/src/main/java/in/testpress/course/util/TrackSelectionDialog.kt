package `in`.testpress.course.util

import `in`.testpress.course.R
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.ui.TrackSelectionView
import com.google.android.exoplayer2.ui.TrackSelectionView.TrackSelectionListener
import com.google.android.exoplayer2.util.Assertions
import com.google.android.material.tabs.TabLayout
import java.util.ArrayList


class TrackSelectionDialog: DialogFragment() {
    private val tabFragments: SparseArray<TrackSelectionViewFragment> = SparseArray()
    private val tabTrackTypes: ArrayList<Int> = ArrayList()

    var titleId = 0
    private var onClickListener: DialogInterface.OnClickListener? = null

    companion object {
        fun createForTrackSelector(trackSelector: DefaultTrackSelector): TrackSelectionDialog? {
            val mappedTrackInfo = Assertions.checkNotNull(trackSelector.currentMappedTrackInfo)
            val trackSelectionDialog = TrackSelectionDialog()
            val parameters = trackSelector.parameters
            trackSelectionDialog.titleId = R.string.select_track
            trackSelectionDialog.init(mappedTrackInfo, parameters,
                DialogInterface.OnClickListener { dialog, which ->
                    val builder = parameters.buildUpon()
                    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                        builder
                            .clearSelectionOverrides(rendererIndex)
                            .setRendererDisabled(rendererIndex, trackSelectionDialog.getIsDisabled(rendererIndex))
                        val overrides = trackSelectionDialog.getOverrides(rendererIndex)
                        if (overrides.isNotEmpty()) {
                            builder.setSelectionOverride(rendererIndex, mappedTrackInfo.getTrackGroups(rendererIndex), overrides[0])
                        }
                    }
                })
            return trackSelectionDialog
        }

        fun createForDownload(mappedTrackInfo: MappedTrackInfo, onClickListener: DialogInterface.OnClickListener): TrackSelectionDialog {
            val trackSelectionDialog = TrackSelectionDialog()
            trackSelectionDialog.titleId = R.string.download
            trackSelectionDialog.init(
                mappedTrackInfo,
                DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS_WITHOUT_CONTEXT,
                onClickListener
            )
            return trackSelectionDialog
        }

        fun hasTracks(mappedTrackInfo: MappedTrackInfo): Boolean {
            for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                if (showTabForRenderer(mappedTrackInfo, rendererIndex)) {
                    return true
                }
            }
            return false
        }

        fun showTabForRenderer(
            mappedTrackInfo: MappedTrackInfo,
            rendererIndex: Int
        ): Boolean {
            val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
            if (trackGroupArray.length == 0) {
                return false
            }
            val trackType = mappedTrackInfo.getRendererType(rendererIndex)
            return isSupportedTrackType(trackType)
        }

        private fun isSupportedTrackType(trackType: Int): Boolean {
            return when (trackType) {
                C.TRACK_TYPE_VIDEO -> true
                else -> false
            }
        }
    }

    fun getIsDisabled(rendererIndex: Int): Boolean {
        val rendererView = tabFragments[rendererIndex]
        return rendererView != null && rendererView.isDisabled
    }

    fun getOverrides(rendererIndex: Int): List<SelectionOverride> {
        val rendererView = tabFragments[rendererIndex]
        return rendererView?.overrides ?: emptyList()
    }

    private fun init(
        mappedTrackInfo: MappedTrackInfo,
        initialParameters: DefaultTrackSelector.Parameters,
        onClickListener: DialogInterface.OnClickListener
    ) {
        this.onClickListener = onClickListener

        for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
            if (showTabForRenderer(mappedTrackInfo, rendererIndex)) {
                val trackType = mappedTrackInfo.getRendererType(rendererIndex)
                val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
                val tabFragment = TrackSelectionViewFragment()
                tabFragment.init(
                    mappedTrackInfo,
                    rendererIndex,
                    initialParameters.getRendererDisabled(rendererIndex),
                    initialParameters.getSelectionOverride(rendererIndex, trackGroupArray)
                )
                tabFragments.put(rendererIndex, tabFragment)
                tabTrackTypes.add(trackType)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.setTheme(R.style.TestpressAppCompatAlertDialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AppCompatDialog(activity, R.style.TestpressAppCompatAlertDialogStyle)
        dialog.setTitle(titleId)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.track_selection_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tabLayout: TabLayout = view.findViewById(R.id.track_selection_dialog_tab_layout)
        val viewPager: ViewPager = view.findViewById(R.id.track_selection_dialog_view_pager)
        val cancelButton = view.findViewById<Button>(R.id.track_selection_dialog_cancel_button)
        val okButton = view.findViewById<Button>(R.id.track_selection_dialog_ok_button)
        viewPager.adapter = FragmentAdapter(childFragmentManager)
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.visibility = if (tabFragments.size() > 1) View.VISIBLE else View.GONE
        cancelButton.setOnClickListener { dismiss() }
        okButton.setOnClickListener {
            onClickListener!!.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
            dismiss()
        }
    }

    private fun getTrackTypeString(
        resources: Resources,
        trackType: Int
    ): String? {
        return when (trackType) {
            C.TRACK_TYPE_VIDEO -> resources.getString(R.string.exo_track_selection_title_video)
            C.TRACK_TYPE_AUDIO -> resources.getString(R.string.exo_track_selection_title_audio)
            C.TRACK_TYPE_TEXT -> resources.getString(R.string.exo_track_selection_title_text)
            else -> throw IllegalArgumentException()
        }
    }

    inner class FragmentAdapter(fragmentManager: FragmentManager?) :
        FragmentPagerAdapter(fragmentManager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return tabFragments.valueAt(position)
        }

        override fun getCount(): Int {
            return tabFragments.size()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return getTrackTypeString(
                resources,
                tabTrackTypes.get(position)
            )
        }
    }

    /** Fragment to show a track seleciton in tab of the track selection dialog.  */
    class TrackSelectionViewFragment : Fragment(),
        TrackSelectionListener {
        private var mappedTrackInfo: MappedTrackInfo? = null
        private var rendererIndex = 0
        private var allowAdaptiveSelections = false
        private var allowMultipleOverrides = false
        var isDisabled = false
        var overrides: List<SelectionOverride> = listOf()

        fun init(
            mappedTrackInfo: MappedTrackInfo?,
            rendererIndex: Int,
            initialIsDisabled: Boolean,
            initialOverride: SelectionOverride?
        ) {
            this.mappedTrackInfo = mappedTrackInfo
            this.rendererIndex = rendererIndex
            isDisabled = initialIsDisabled
            overrides = initialOverride?.let { listOf(it) } ?: emptyList()
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView: View = inflater.inflate(
                R.layout.exo_track_selection_dialog, container,  /* attachToRoot= */false
            )
            val trackSelectionView: TrackSelectionView =
                rootView.findViewById(R.id.exo_track_selection_view)
            trackSelectionView.setShowDisableOption(true)
            trackSelectionView.setAllowMultipleOverrides(allowMultipleOverrides)
            trackSelectionView.setAllowAdaptiveSelections(allowAdaptiveSelections)
            trackSelectionView.init(
                mappedTrackInfo!!, rendererIndex, isDisabled, overrides,  /* listener= */this
            )
            return rootView
        }

        override fun onTrackSelectionChanged(isDisabled: Boolean, overrides: List<SelectionOverride>) {
            this.isDisabled = isDisabled
            this.overrides = overrides
        }

        init {
            // Retain instance across activity re-creation to prevent losing access to init data.
            retainInstance = true
        }
    }
}