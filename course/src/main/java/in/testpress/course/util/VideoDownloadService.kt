package `in`.testpress.course.util

import `in`.testpress.course.CourseApplication
import `in`.testpress.course.R
import android.app.Notification
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.PlatformScheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util

/** A service for downloading media.  */
class VideoDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.download,
    R.string.download_description
) {
    private var notificationHelper: DownloadNotificationHelper? = null
    override fun onCreate() {
        super.onCreate()
        notificationHelper =
            DownloadNotificationHelper(this, CHANNEL_ID)
    }

    override fun getDownloadManager(): DownloadManager {
        return (application as CourseApplication).getDownloadManager()
    }

    override fun getScheduler(): PlatformScheduler? {
        return if (Util.SDK_INT >= 21) PlatformScheduler(
            this,
            JOB_ID
        ) else null
    }

    override fun getForegroundNotification(downloads: List<Download>): Notification {
        return notificationHelper!!.buildProgressNotification(
            R.drawable.ic_download,  null,  null, downloads
        )
    }

    override fun onDownloadChanged(download: Download) {
        val notification: Notification = when (download.state) {
            Download.STATE_COMPLETED -> {
                notificationHelper!!.buildDownloadCompletedNotification(
                    R.drawable.ic_download_done,  /* contentIntent= */
                    null,
                    Util.fromUtf8Bytes(download.request.data)
                )
            }
            Download.STATE_FAILED -> {
                notificationHelper!!.buildDownloadFailedNotification(
                    R.drawable.ic_download_done,  /* contentIntent= */
                    null,
                    Util.fromUtf8Bytes(download.request.data)
                )
            }
            else -> {
                return
            }
        }
        NotificationUtil.setNotification(
            this,
            nextNotificationId++,
            notification
        )
    }

    companion object {
        private const val CHANNEL_ID = "download_channel"
        private const val JOB_ID = 1
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private var nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1
    }
}