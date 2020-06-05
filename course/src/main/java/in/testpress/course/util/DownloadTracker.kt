package `in`.testpress.course.util

import `in`.testpress.course.R
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.offline.StreamKey
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import java.io.IOException
import java.util.HashMap
import java.util.concurrent.CopyOnWriteArraySet

class DownloadTracker(
    val context: Context,
    val dataSourceFactory: DataSource.Factory,
    val downloadManager: DownloadManager
) {
    val downloadIndex = downloadManager.downloadIndex
    var downloads: HashMap<Uri, Download> = HashMap()
    var startDownloadDialogHelper: StartDownloadDialogHelper? = null
    private val listeners: CopyOnWriteArraySet<Listener> = CopyOnWriteArraySet()

    init {
        downloadManager.addListener(DownloadManagerListener())
        loadDownloads()
    }

    fun loadDownloads() {
        try {
            downloadIndex.getDownloads().use { loadedDownloads ->
                while (loadedDownloads.moveToNext()) {
                    val download = loadedDownloads.download
                    downloads[download.request.uri] = download
                }
            }
        } catch (e: IOException) {
           Log.d("DownloadTracker", "Failed to query downloads")
        }
    }

    fun isDownloaded(uri: Uri): Boolean {
        val download = downloads.get(uri)
        return download != null && download.state == Download.STATE_COMPLETED
    }

    fun getOfflineStreamKeys(uri: Uri?): List<StreamKey> {
        val download = downloads[uri]
        return if (download != null && download.state != Download.STATE_FAILED) download.request.streamKeys else emptyList()
    }


    fun toggleDownload(
        fragmentManager: FragmentManager,
        name: String,
        uri: Uri,
        extension: String,
        renderersFactory: RenderersFactory
    ) {
        val download = downloads[uri]
        if (download != null) {
            DownloadService.sendRemoveDownload(
                context,
                VideoDownloadService::class.java,
                download.request.id,  /* foreground= */
                false
            )
        } else {
            if (startDownloadDialogHelper != null) {
                startDownloadDialogHelper?.release()
            }

            startDownloadDialogHelper = StartDownloadDialogHelper(
                fragmentManager, getDownloadHelper(uri, extension, renderersFactory), name!!
            )
        }
    }

    private fun getDownloadHelper(
        uri: Uri, extension: String, renderersFactory: RenderersFactory
    ): DownloadHelper {
        val type = Util.inferContentType(uri, extension)
        return when (type) {
            C.TYPE_DASH -> DownloadHelper.forDash(
                context,
                uri,
                dataSourceFactory,
                renderersFactory
            )
            C.TYPE_SS -> DownloadHelper.forSmoothStreaming(
                context,
                uri,
                dataSourceFactory,
                renderersFactory
            )
            C.TYPE_HLS -> DownloadHelper.forHls(
                context,
                uri,
                dataSourceFactory,
                renderersFactory
            )
            C.TYPE_OTHER -> DownloadHelper.forProgressive(context, uri)
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }

    inner class DownloadManagerListener: DownloadManager.Listener {
        override fun onDownloadChanged(downloadManager: DownloadManager, download: Download) {
            downloads.put(download.request.uri, download)
            for(listener in listeners) {
                listener.onDownloadsChanged()
            }
        }

        override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
            downloads.remove(download.request.uri)
            for(listener in listeners) {
                listener.onDownloadsChanged()
            }
        }
    }

    inner class StartDownloadDialogHelper(
        val fragmentManager: FragmentManager,
        val downloadHelper: DownloadHelper,
        val name: String
    ): DownloadHelper.Callback, DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener {

        lateinit var mappedTrackInfo: MappingTrackSelector.MappedTrackInfo
        var trackSelectionDialog: TrackSelectionDialog? = null

        init {
            downloadHelper.prepare(this)
        }

        fun release() {
            downloadHelper.release()
            if (trackSelectionDialog != null) {
                trackSelectionDialog!!.dismiss()
            }
        }

        override fun onPrepared(helper: DownloadHelper) {
            if(helper.periodCount == 0) {
                startDownload()
                downloadHelper.release()
                return
            }
            mappedTrackInfo = downloadHelper.getMappedTrackInfo(0)

            if (!TrackSelectionDialog.hasTracks(mappedTrackInfo)) {
                startDownload()
                downloadHelper.release()
            }
            trackSelectionDialog = TrackSelectionDialog.createForDownload(
                mappedTrackInfo, this
            )
            trackSelectionDialog?.show(fragmentManager, null)
        }

        override fun onPrepareError(helper: DownloadHelper, e: IOException) {
            Toast.makeText(
                context.applicationContext, R.string.download_start_error, Toast.LENGTH_LONG
            ).show()
        }

        override fun onClick(dialog: DialogInterface?, which: Int) {
            for(periodIndex in 0 until downloadHelper.periodCount) {
                downloadHelper.clearTrackSelections(periodIndex)
                for(rendererIndex in 0 until mappedTrackInfo.rendererCount) {
                    if (trackSelectionDialog?.getIsDisabled(rendererIndex) == false) {
                        downloadHelper.addTrackSelectionForSingleRenderer(
                            periodIndex,
                            rendererIndex,
                            DownloadHelper.DEFAULT_TRACK_SELECTOR_PARAMETERS_WITHOUT_CONTEXT,
                            trackSelectionDialog!!.getOverrides(rendererIndex)
                        )
                    }
                }

                val downloadRequest = buildDownloadRequest()
                if (downloadRequest.streamKeys.isEmpty()) {
                    return
                }
                startDownload(downloadRequest)
            }
        }

        override fun onDismiss(p0: DialogInterface?) {
            trackSelectionDialog = null
            downloadHelper.release()
        }

        private fun startDownload() {
            startDownload(buildDownloadRequest())
        }

        private fun buildDownloadRequest(): DownloadRequest {
            return downloadHelper.getDownloadRequest(Util.getUtf8Bytes(name))
        }

        private fun startDownload(downloadRequest: DownloadRequest) {
            DownloadService.sendAddDownload(
                context, VideoDownloadService::class.java, downloadRequest,  /* foreground= */false
            )
        }
    }

    interface Listener {
        /** Called when the tracked downloads changed.  */
        fun onDownloadsChanged()
    }
}