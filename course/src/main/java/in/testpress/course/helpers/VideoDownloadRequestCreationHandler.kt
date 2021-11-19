package `in`.testpress.course.helpers

import `in`.testpress.course.util.ExoPlayerDataSourceFactory
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.VideoUtils.getLowBitrateTrackIndex
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.util.Util
import java.io.IOException

class VideoDownloadRequestCreationHandler(val context: Context, val url: String, val name: String) :
    DownloadHelper.Callback {
    private val downloadHelper: DownloadHelper
    private val trackSelectionParameters: DefaultTrackSelector.Parameters
    var listener: Listener? = null
    private val mediaItem: MediaItem

    init {
        trackSelectionParameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
        mediaItem = MediaItem.Builder()
            .setUri(url)
            .setDrmUuid(C.WIDEVINE_UUID)
            .setDrmMultiSession(true)
            .build()
        downloadHelper = getDownloadHelper()
        downloadHelper.prepare(this)
    }

    private fun getDownloadHelper(): DownloadHelper {
        val dataSourceFactory = ExoPlayerDataSourceFactory(context).build()
        val renderersFactory = DefaultRenderersFactory(context)
        return DownloadHelper.forMediaItem(mediaItem, trackSelectionParameters, renderersFactory, dataSourceFactory)
    }

    override fun onPrepared(helper: DownloadHelper) {
        listener?.onDownloadRequestHandlerPrepared(
            getMappedTrackInfo(),
            getRendererIndex(),
            getTrackSelectionOverrides()
        )
    }

    private fun getMappedTrackInfo(): MappingTrackSelector.MappedTrackInfo {
        return downloadHelper.getMappedTrackInfo(0)
    }

    private fun getRendererIndex(): Int {
        return ExoPlayerUtil.getRendererIndex(C.TRACK_TYPE_VIDEO, getMappedTrackInfo())
    }

    private fun getTrackSelectionOverrides(): List<DefaultTrackSelector.SelectionOverride> {
        val trackGroups = getMappedTrackInfo().getTrackGroups(getRendererIndex())
        if (trackGroups.length == 0) {
            return emptyList()
        }
        val (lowBandwithTrackIndex, lowBandwithGroupIndex) = getLowBitrateTrackIndex(trackGroups)
        return listOf(DefaultTrackSelector.SelectionOverride(lowBandwithGroupIndex, lowBandwithTrackIndex))
    }

    override fun onPrepareError(helper: DownloadHelper, e: IOException) {
        listener?.onDownloadRequestHandlerPrepareError(helper, e)
    }

    fun buildDownloadRequest(overrides: List<DefaultTrackSelector.SelectionOverride>): DownloadRequest {
        setSelectedTracks(overrides)
        return downloadHelper.getDownloadRequest(Util.getUtf8Bytes(name))
    }

    private fun setSelectedTracks(overrides: List<DefaultTrackSelector.SelectionOverride>) {
        val mappedTrackInfo = downloadHelper.getMappedTrackInfo(0)
        for (index in 0 until downloadHelper.periodCount) {
            downloadHelper.clearTrackSelections(index)
            val rendererIndex = ExoPlayerUtil.getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo)
            downloadHelper.addTrackSelectionForSingleRenderer(
                index,
                rendererIndex,
                DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS_WITHOUT_CONTEXT,
                overrides
            )
        }
    }

    interface Listener {
        fun onDownloadRequestHandlerPrepared(
            mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
            rendererIndex: Int,
            overrides: List<DefaultTrackSelector.SelectionOverride>
        )

        fun onDownloadRequestHandlerPrepareError(helper: DownloadHelper, e: IOException)
    }
}