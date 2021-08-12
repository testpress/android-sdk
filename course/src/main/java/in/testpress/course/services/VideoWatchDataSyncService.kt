package `in`.testpress.course.services

import `in`.testpress.course.repository.VideoWatchDataRepository
import android.app.IntentService
import android.content.Intent
import android.util.Log

class VideoWatchDataSyncService: IntentService(VideoWatchDataSyncService::class.simpleName) {
    override fun onHandleIntent(intent: Intent?) {
        Log.d("VideoWatchDataSyncServi", "onHandleIntent: ")
        val repository = VideoWatchDataRepository(application)
        repository.syncData()
    }
}