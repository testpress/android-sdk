package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.VideoWatchDataEntity
import `in`.testpress.models.greendao.Content
import android.content.Context
import android.os.AsyncTask

class VideoWatchDataRepository(val context: Context) {
    val dao = TestpressDatabase(context).videoWatchDataDao()
    val TAG = "VideoWatchDataRepositor"

    fun saveData(content: Content, videoAttemptParameters: MutableMap<String, Any>) {
        val lastWatchPosition = videoAttemptParameters[TestpressCourseApiClient.LAST_POSITION] as? String
        val timeRanges = videoAttemptParameters[TestpressCourseApiClient.TIME_RANGES] as? ArrayList<Array<String>>
        val entity = VideoWatchDataEntity(
                null,
                lastWatchPosition,
                timeRanges!!,
                content.id,
                content.videoId
        )
        AsyncTask.execute {
            val dbEntity = dao.get(content.id)
            dbEntity?.let {
                entity.id = it.id
                timeRanges.addAll(it.watchedTimeRanges)
                entity.watchedTimeRanges = timeRanges.distinct()
            }
            dao.insert(entity)
        }
    }

    fun syncData() {
        AsyncTask.execute {
            val videoWatchData = dao.getAll()
            if (videoWatchData.isNotEmpty()) {
                val courseNetwork = CourseNetwork(context)
                courseNetwork.syncVideoWatchData(videoWatchData)
                        .enqueue(object : TestpressCallback<Void>(){
                            override fun onSuccess(result: Void?) {
                                dao.deleteAll()
                            }

                            override fun onException(exception: TestpressException?) {}
                        })
            }
        }

    }
}