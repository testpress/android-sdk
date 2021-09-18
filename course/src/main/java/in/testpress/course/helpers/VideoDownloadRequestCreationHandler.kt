package `in`.testpress.course.helpers

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.util.DRMLicenseFetchCallback
import `in`.testpress.course.util.ExoPlayerDataSourceFactory
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.OfflineDRMLicenseHelper
import `in`.testpress.course.util.VideoUtils.getAudioOrVideoInfoWithDrmInitData
import android.content.Context
import android.net.Uri
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
        val uri = Uri.parse(url)
        trackSelectionParameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
        mediaItem = MediaItem.Builder()
            .setUri(url)
            .setDrmUuid(C.WIDEVINE_UUID)
            .setDrmMultiSession(true)
            .build()
        downloadHelper = getDownloadHelper(uri)
        downloadHelper.prepare(this)
    }

    private fun getDownloadHelper(uri: Uri): DownloadHelper {
        val sessionManager = DefaultDrmSessionManager.Builder()
            .build(CustomHttpDrmMediaCallback(context, content.id))
        val dataSourceFactory = ExoPlayerDataSourceFactory(context).build()
        val renderersFactory = DefaultRenderersFactory(context)
        sessionManager.setMode(DefaultDrmSessionManager.MODE_DOWNLOAD, null)
        return DownloadHelper.forMediaItem(mediaItem, trackSelectionParameters, renderersFactory, dataSourceFactory, sessionManager)
    }

    override fun onPrepared(helper: DownloadHelper) {
        val format = getAudioOrVideoInfoWithDrmInitData(helper)
        if (format == null) {
            listener?.onDownloadRequestHandlerPrepared(
                getMappedTrackInfo(),
                getRendererIndex(),
                getTrackSelectionOverrides()
            )
            return
        }

        if (!hasSchemaData(format.drmInitData!!)) {
            Toast.makeText(
                context,
                "Download Start Error",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        OfflineDRMLicenseHelper.fetchLicense(context, content.id, downloadHelper, this)
    }

    private fun hasSchemaData(drmInitData: DrmInitData): Boolean {
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
        val trackGroup = getMappedTrackInfo().getTrackGroups(getRendererIndex())
        val selectionOverrides =
            trackSelectionParameters.getSelectionOverride(getRendererIndex(), trackGroup)
        return selectionOverrides?.let { listOf(it) } ?: emptyList()
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