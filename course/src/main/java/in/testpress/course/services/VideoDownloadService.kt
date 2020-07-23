package `in`.testpress.course.services

import `in`.testpress.course.R
import `in`.testpress.course.helpers.VideoDownloadManager
import android.app.Notification
import android.content.Context
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util

class VideoDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.download,
    R.string.download_description
), DownloadManager.Listener {

    private lateinit var notificationHelper: DownloadNotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper = DownloadNotificationHelper(this, CHANNEL_ID)
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
        when (download.state) {
            Download.STATE_COMPLETED -> {
                val message = "Download is completed"
                notification = notificationHelper.buildDownloadCompletedNotification(
                    R.drawable.ic_download_done,
                    null,
                    message
                )
            }
            Download.STATE_FAILED -> {
                val message = "Download is failed. Please try again"
                notification = notificationHelper.buildDownloadFailedNotification(
                    R.drawable.ic_download_done,
                    null,
                    message
                )
            }
        }
        NotificationUtil.setNotification(this, nextNotificationId, notification)
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
}