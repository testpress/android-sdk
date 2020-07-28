package `in`.testpress.course.repository

import `in`.testpress.course.helpers.VideoDownloadManager
import `in`.testpress.database.OfflineVideo
import `in`.testpress.database.TestpressDatabase
import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.exoplayer2.offline.Download

class OfflineVideoRepository(val context: Context) {
    private val offlineVideoDao = TestpressDatabase(context).offlineVideoDao()
    val offlineVideos = offlineVideoDao.getAll()
    private val downloadManager = VideoDownloadManager(context).get()

    fun refreshCurrentDownloadsStatus() {
        for (download in downloadManager.currentDownloads) {
            updateDownloadStatus(download)
        }
    }

    fun updateDownloadStatus(download: Download) {
        val offlineVideo = offlineVideoDao.getByUrl(download.request.uri.toString())
        offlineVideo?.let {
            offlineVideo.percentageDownloaded = download.percentDownloaded.toInt()
            offlineVideo.bytesDownloaded = download.bytesDownloaded
            offlineVideo.totalSize = download.contentLength
            offlineVideoDao.insert(offlineVideo)
        }
    }

    fun delete(download: Download) {
        offlineVideoDao.deleteByUrl(download.request.uri.toString())
    }

    fun get(url: String): LiveData<OfflineVideo?> {
        return offlineVideoDao.get(url)
    }

    fun getUrls(): LiveData<List<String>> {
        return offlineVideoDao.getUrls()
    }
}