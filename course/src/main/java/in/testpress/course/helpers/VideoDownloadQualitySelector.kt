package `in`.testpress.course.helpers

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.util.ExoPlayerDataSourceFactory
import `in`.testpress.course.util.ExoPlayerUtil
import `in`.testpress.course.util.TrackSelectionDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.util.Util
import java.io.IOException

typealias OnSubmitListener = (DownloadRequest) -> Unit

class VideoDownloadQualitySelector(
    val fragmentManager: FragmentManager,
    val context: Context,
    val content: DomainContent
) : DownloadHelper.Callback, DialogInterface.OnClickListener {

    private val downloadHelper: DownloadHelper
    val trackSelectionParameters = DownloadHelper.getDefaultTrackSelectorParameters(context)
    private lateinit var trackSelectionDialog: TrackSelectionDialog
    private var onSubmitListener: OnSubmitListener? = null

    init {
        val uri = Uri.parse(content.video!!.hlsUrl())
        downloadHelper = getDownloadHelper(uri)
        downloadHelper.prepare(this)
    }

    private fun getDownloadHelper(uri: Uri): DownloadHelper {
        val dataSourceFactory = ExoPlayerDataSourceFactory(context).build()
        val renderersFactory = DefaultRenderersFactory(context)
        return when (val type = Util.inferContentType(uri)) {
            C.TYPE_HLS -> DownloadHelper.forHls(context, uri, dataSourceFactory, renderersFactory)
            C.TYPE_OTHER -> DownloadHelper.forProgressive(context, uri)
            else -> throw IllegalStateException("Video type not supported for download $type")
        }
    }

    override fun onPrepared(helper: DownloadHelper) {
        showTrackSelectionDialog()
    }

    override fun onPrepareError(helper: DownloadHelper, e: IOException) {
        Toast.makeText(context, "Could not start download. Please try again", Toast.LENGTH_LONG)
            .show()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        setSelectedTracks()
        onSubmitListener?.invoke(buildDownloadRequest())
    }

    fun setOnSubmitListener(listener: OnSubmitListener) {
        onSubmitListener = listener
    }

    private fun showTrackSelectionDialog() {
        trackSelectionDialog =
            TrackSelectionDialog(trackSelectionParameters, downloadHelper.getMappedTrackInfo(0))
        trackSelectionDialog.onClickListener = this
        trackSelectionDialog.show(fragmentManager, null)
    }

    private fun setSelectedTracks() {
        val mappedTrackInfo = downloadHelper.getMappedTrackInfo(0)
        for (index in 0 until downloadHelper.periodCount) {
            downloadHelper.clearTrackSelections(index)
            val rendererIndex = ExoPlayerUtil.getRendererIndex(C.TRACK_TYPE_VIDEO, mappedTrackInfo)
            downloadHelper.addTrackSelectionForSingleRenderer(
                index,
                rendererIndex,
                DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS_WITHOUT_CONTEXT,
                trackSelectionDialog.overrides
            )
        }
    }

    private fun buildDownloadRequest(): DownloadRequest {
        return downloadHelper.getDownloadRequest(Util.getUtf8Bytes(content.title!!))
    }
}

class VideoQualityChooserDialog(uri: String): DialogFragment() {
    private val downloadHelper: DownloadHelper

    init {
        val uri = Uri.parse(uri)
        downloadHelper = getDownloadHelper(uri)
        downloadHelper.prepare(this)
    }

    private fun getDownloadHelper(uri: Uri): DownloadHelper {
        val dataSourceFactory = ExoPlayerDataSourceFactory(context).build()
        val renderersFactory = DefaultRenderersFactory(context)
        return when (val type = Util.inferContentType(uri)) {
            C.TYPE_HLS -> DownloadHelper.forHls(context, uri, dataSourceFactory, renderersFactory)
            C.TYPE_OTHER -> DownloadHelper.forProgressive(context, uri)
            else -> throw IllegalStateException("Video type not supported for download $type")
        }
    }
}
