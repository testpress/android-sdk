package `in`.testpress.course.repository

import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.VideoSyncStatus
import `in`.testpress.models.greendao.Content
import android.content.Context
import android.os.AsyncTask

class VideoWatchDataRepository(val context: Context) {
    private val offlineVideoDao = TestpressDatabase(context).offlineVideoDao()
    val TAG = "VideoWatchDataRepositor"

    fun saveData(content: Content, videoAttemptParameters: MutableMap<String, Any>) {
        val lastWatchPosition = videoAttemptParameters[TestpressCourseApiClient.LAST_POSITION] as? String
        val timeRanges = videoAttemptParameters[TestpressCourseApiClient.TIME_RANGES] as? ArrayList<Array<String>> ?: arrayListOf()

        AsyncTask.execute {
            val offlineVideo = offlineVideoDao.getByContentId(content.id)
            offlineVideo?.let {
                timeRanges.addAll(it.watchedTimeRanges)
                offlineVideo.watchedTimeRanges = timeRanges.distinct()
                offlineVideo.lastWatchPosition = lastWatchPosition
                offlineVideoDao.insert(offlineVideo)
            }
        }
    }

    suspend fun syncData() {
        val courseNetwork = CourseNetwork(context)
        val videoWatchData = offlineVideoDao.getAllSync()
            videoWatchData.forEach {
                it.syncState = VideoSyncStatus.SYNCING
                offlineVideoDao.update(it)

//                val queryParams = hashMapOf<String, Any>("page" to )
                val response = courseNetwork.syncVideoWatchData(hashMapOf()).execute()
                if (response.isSuccessful) {
                    it.syncState = VideoSyncStatus.SUCCESS
                    offlineVideoDao.update(it)
                }
            }
    }
}