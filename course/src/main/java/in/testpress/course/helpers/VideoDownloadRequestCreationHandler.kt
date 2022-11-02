package `in`.testpress.course.helpers

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.util.DRMLicenseFetchCallback
import `in`.testpress.course.util.ExoPlayerDataSourceFactory
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.OfflineDRMLicenseHelper
import `in`.testpress.course.util.VideoUtils.getAudioOrVideoInfoWithDrmInitData
import `in`.testpress.course.util.VideoUtils.getLowBitrateTrackIndex
import android.content.Context
import android.widget.Toast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.DrmInitData
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

import com.google.android.exoplayer2.source.TrackGroupArray




class VideoDownloadRequestCreationHandler(
    val context: Context,
    val content: DomainContent
) :
    DownloadHelper.Callback, DRMLicenseFetchCallback {
    private val downloadHelper: DownloadHelper
    private val trackSelectionParameters: DefaultTrackSelector.Parameters
    var listener: Listener? = null
    private val mediaItem:MediaItem
    private var keySetId:ByteArray? = null

    init {
        val url = content.video!!.getPlaybackURL()!!
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
        val sessionManager = DefaultDrmSessionManager.Builder()
            .build(CustomHttpDrmMediaCallback(context, content.id))
        sessionManager.setMode(DefaultDrmSessionManager.MODE_DOWNLOAD, null)
        val dataSourceFactory = ExoPlayerDataSourceFactory(context).build()
        val renderersFactory = DefaultRenderersFactory(context)
        return DownloadHelper.forMediaItem(mediaItem, trackSelectionParameters, renderersFactory, dataSourceFactory, sessionManager)
    }

    override fun onPrepared(helper: DownloadHelper) {
        val videoOrAudioData = getAudioOrVideoInfoWithDrmInitData(helper)
        val isDRMProtectedVideo = videoOrAudioData != null
        if (isDRMProtectedVideo) {
            if (hasDRMSchemaData(videoOrAudioData!!.drmInitData!!)) {
                OfflineDRMLicenseHelper.fetchLicense(context, content.id, downloadHelper, this)
            } else {
                Toast.makeText(
                    context,
                    "Error in downloading video",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        listener?.onDownloadRequestHandlerPrepared(
            getMappedTrackInfo(),
            getRendererIndex(),
            getTrackSelectionOverrides()
        )
    }

    private fun hasDRMSchemaData(drmInitData: DrmInitData): Boolean {
        for (i in 0 until drmInitData.schemeDataCount) {
            if (drmInitData[i].hasData()) {
                return true
            }
        }
        return false
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
        val name = content.title ?: ""
        return downloadHelper.getDownloadRequest(Util.getUtf8Bytes(name)).copyWithKeySetId(keySetId)
    }

    private fun setSelectedTracks(overrides: List<DefaultTrackSelector.SelectionOverride>) {
        val mappedTrackInfo = downloadHelper.getMappedTrackInfo(0)
        for (index in 0 until downloadHelper.periodCount) {
            downloadHelper.clearTrackSelections(index)
            var builder = DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS_WITHOUT_CONTEXT.buildUpon()
            val videoRendererIndex = ExoPlayerUtil.getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo)
            val trackGroupArray: TrackGroupArray = mappedTrackInfo.getTrackGroups(videoRendererIndex)
            for (i in overrides.indices) {
                builder.setSelectionOverride(videoRendererIndex, trackGroupArray, overrides[i])
                downloadHelper.addTrackSelection(index, builder.build())
            }
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

    override fun onLicenseFetchSuccess(keySetId: ByteArray) {
        CoroutineScope(Dispatchers.Main).launch {
            listener?.onDownloadRequestHandlerPrepared(
                getMappedTrackInfo(),
                getRendererIndex(),
                getTrackSelectionOverrides()
            )
        }
    }

    override fun onLicenseFetchFailure() {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                context,
                "Error in starting video download (License fetch error)",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}