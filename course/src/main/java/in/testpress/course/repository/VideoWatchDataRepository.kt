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

    fun syncData() {
        val courseNetwork = CourseNetwork(context)
        offlineVideoDao.getAllSync().forEach {
            it.syncState = VideoSyncStatus.SYNCING
            offlineVideoDao.update(it)

            val queryParams = hashMapOf(
                "watched_time_ranges" to it.watchedTimeRanges,
                "chapter_content_id" to it.contentId!!,
                "last_watch_position" to it.lastWatchPosition!!
            )
            val response = courseNetwork.syncVideoWatchData(it.contentId!!, queryParams).execute()
            if (response.isSuccessful) {
                it.syncState = VideoSyncStatus.SUCCESS
                offlineVideoDao.update(it)
            }
        }
    }
}