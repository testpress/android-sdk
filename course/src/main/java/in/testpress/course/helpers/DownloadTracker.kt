package `in`.testpress.course.helpers

import `in`.testpress.course.services.VideoDownloadService
import android.content.Context
import com.google.android.exoplayer2.offline.DownloadService

class DownloadTracker(val context: Context) {
    fun pauseAll() {
        DownloadService.sendPauseDownloads(context, VideoDownloadService::class.java, false)
    }

    fun resumeAll() {
        DownloadService.sendResumeDownloads(context, VideoDownloadService::class.java, false)
    }

    fun removeAll() {
        DownloadService.sendRemoveAllDownloads(context, VideoDownloadService::class.java, false)
    }
}