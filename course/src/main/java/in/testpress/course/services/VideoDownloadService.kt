package `in`.testpress.course.services

import `in`.testpress.course.R
import `in`.testpress.course.helpers.VideoDownloadManager
import `in`.testpress.course.helpers.VideoDownloadMonitor
import `in`.testpress.course.repository.OfflineVideoRepository
import android.app.Notification
import android.content.Context
import android.util.Log
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class VideoDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.download,
    R.string.download_description
), DownloadManager.Listener, VideoDownloadMonitor.Callback {

    private lateinit var offlineVideoRepository: OfflineVideoRepository

    private lateinit var notificationHelper: DownloadNotificationHelper
    lateinit var downloadMonitor: VideoDownloadMonitor

    override fun onCreate() {
        super.onCreate()
        offlineVideoRepository = OfflineVideoRepository(this)
        notificationHelper = DownloadNotificationHelper(this, CHANNEL_ID)
        initializeDownloadMonitor()
    }

    private fun initializeDownloadMonitor() {
        downloadMonitor = VideoDownloadMonitor()
        downloadMonitor.callback = this
    }

    override fun getDownloadManager(): DownloadManager {
        val downloadManager = VideoDownloadManager(this).get()
        downloadManager.addListener(this)
        return downloadManager
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        // TODO : Add intent and also download message
        return notificationHelper.buildProgressNotification(
            R.drawable.ic_download,
            null,
            null,
            downloads
        )
    }

    override fun getScheduler(): Scheduler? {
        return if (Util.SDK_INT >= 21) PlatformScheduler(
            this,
            JOB_ID
        ) else null
    }

    override fun onDownloadChanged(downloadManager: DownloadManager, download: Download) {
        var notification: Notification? = null

        if (!downloadMonitor.isRunning()) {
            downloadMonitor.start()
        }

        when (download.state) {
            Download.STATE_COMPLETED -> {
                downloadMonitor.updateVideoProgress(download)
                notification = getCompletedNotification()
            }
            Download.STATE_FAILED -> notification = getFailedNotification()
            Download.STATE_REMOVING -> downloadMonitor.deleteVideo(download)
        }
        NotificationUtil.setNotification(this, nextNotificationId, notification)
    }

    private fun getFailedNotification(): Notification {
        val message = "Download is failed. Please try again"
        return notificationHelper.buildDownloadFailedNotification(
            R.drawable.ic_download_done,
            null,
            message
        )
    }

    private fun getCompletedNotification(): Notification {
        val message = "Download is completed"
        return notificationHelper.buildDownloadCompletedNotification(
            R.drawable.ic_download_done,
            null,
            message
        )
    }

    companion object {
        private const val CHANNEL_ID = "download_channel"
        private const val JOB_ID = 1
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private var nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1

        fun start(context: Context) {
            try {
                start(context, VideoDownloadService::class.java)
            } catch (e: IllegalStateException) {
                startForeground(context, VideoDownloadService::class.java)
            }
        }
    }

    override suspend fun onCurrentDownloadsUpdate() {
        withContext(Dispatchers.IO) {
            while (downloadManager.currentDownloads.isNotEmpty()) {
                offlineVideoRepository.refreshCurrentDownloadsStatus()
                delay(1000)
            }
            downloadMonitor.stop()
        }
    }

    override suspend fun onUpdate(download: Download) {
        withContext(Dispatchers.IO) {
            offlineVideoRepository.updateDownloadStatus(download)
        }
    }

    override suspend fun onDelete(download: Download) {
        withContext(Dispatchers.IO) {
            offlineVideoRepository.delete(download)
        }
    }
}