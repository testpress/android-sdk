package `in`.testpress.course.repository

import `in`.testpress.course.helpers.VideoDownloadManager
import `in`.testpress.database.TestpressDatabase
import android.content.Context
import com.google.android.exoplayer2.offline.Download

class OfflineVideoRepository(val context: Context) {
    private val offlineVideoDao = TestpressDatabase(context).offlineVideoDao()
    val offlineVideos = offlineVideoDao.getAll()
    private val downloadManager = VideoDownloadManager(context).get()

    fun refreshCurrentDownloadsProgress() {
        for (download in downloadManager.currentDownloads) {
            updateOfflineVideoDownloadStatus(download)
        }
    }

    fun updateOfflineVideoDownloadStatus(download: Download) {
        val offlineVideo = offlineVideoDao.getByUrl(download.request.uri.toString())
        offlineVideo?.let {
            offlineVideo.percentageDownloaded = download.percentDownloaded.toInt()
            offlineVideo.bytesDownloaded = download.bytesDownloaded
            offlineVideo.totalSize = download.contentLength
            offlineVideoDao.insert(offlineVideo)
        }
    }

    fun deleteOfflineVideo(download: Download) {
        offlineVideoDao.deleteByUrl(download.request.uri.toString())
    }
}