package `in`.testpress.course.helpers

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.services.VideoDownloadService
import `in`.testpress.database.OfflineVideo
import `in`.testpress.database.TestpressDatabase
import android.content.Context
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadTask(val url: String, val context: Context) {
    private val downloadManager = VideoDownloadManager(context).get()
    private val downloadIndex = downloadManager.downloadIndex

    fun start(downloadRequest: DownloadRequest, content: DomainContent) {
        storeDownloadInfo(content)
        DownloadService.sendAddDownload(
            context,
            VideoDownloadService::class.java,
            downloadRequest,
            false
        )
    }

    private fun storeDownloadInfo(content: DomainContent) {
        val offlineVideo = OfflineVideo(
            title = content.title,
            description = content.description,
            duration = content.video?.duration ?: "0",
            url = content.video?.hlsUrl(),
            contentId = content.id,
            remoteThumbnail = content.coverImageMedium,
            courseId = content.courseId
        )
        val offlineVideoDao = TestpressDatabase(context).offlineVideoDao()

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                offlineVideoDao.insert(offlineVideo)
            }
        }
    }

    fun pause() {
        val download = downloadIndex.getDownload(url)
        val STOP_REASON_PAUSED = 1
        download?.let {
            DownloadService.sendSetStopReason(
                context,
                VideoDownloadService::class.java,
                download.request.id,
                STOP_REASON_PAUSED,
                false
            )
        }
    }

    fun resume() {
        val download = downloadIndex.getDownload(url)
        download?.let {
            DownloadService.sendSetStopReason(
                context,
                VideoDownloadService::class.java,
                download.request.id,
                Download.STOP_REASON_NONE,
                false
            )
        }
    }

    fun delete() {
        val download = downloadIndex.getDownload(url)
        download?.let {
            DownloadService.sendRemoveDownload(
                context,
                VideoDownloadService::class.java,
                download.request.id,
                false
            )
        }
    }

    fun getProgressPercentage(): Int {
        val download = downloadIndex.getDownload(url)

        download?.let {
            return download.percentDownloaded.toInt()
        }

        return -1
    }

    fun isDownloaded(): Boolean {
        val download = downloadIndex.getDownload(url)
        return download != null && download.state == Download.STATE_COMPLETED
    }

    fun isBeingDownloaded(): Boolean {
        val download = downloadIndex.getDownload(url)
        return download != null && download.state == Download.STATE_DOWNLOADING
    }

}

object VideoDownload {
    @JvmStatic
    fun getDownloadRequest(url: String, context: Context): DownloadRequest? {
        val downloadManager = VideoDownloadManager(context).get()
        val downloadIndex = downloadManager.downloadIndex
        val download = downloadIndex.getDownload(url)
        return if (download != null && download.state != Download.STATE_FAILED) download.request else null
    }
}