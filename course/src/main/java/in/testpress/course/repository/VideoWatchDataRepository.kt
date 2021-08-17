package `in`.testpress.course.repository

import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.VideoSyncStatus
import `in`.testpress.models.greendao.Content
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class VideoWatchDataRepository(val context: Context) {
    val dao = TestpressDatabase(context).videoWatchDataDao()
    val offlineVideoDao = TestpressDatabase(context).offlineVideoDao()
    val TAG = "VideoWatchDataRepositor"

    fun saveData(content: Content, videoAttemptParameters: MutableMap<String, Any>) {


        val lastWatchPosition = videoAttemptParameters[TestpressCourseApiClient.LAST_POSITION] as? String
        val timeRanges = videoAttemptParameters[TestpressCourseApiClient.TIME_RANGES] as? ArrayList<Array<String>> ?: arrayListOf()

        Log.d(TAG, "saveData: $timeRanges")
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
        AsyncTask.execute {
            val videoWatchData = offlineVideoDao.getAllSync()
            videoWatchData.forEach {
                it.syncState = VideoSyncStatus.SYNCING
                offlineVideoDao.update(it)


//                Timer().schedule(2000) {
//                    it.syncState = VideoSyncStatus.SUCCESS
//                    offlineVideoDao.update(it)
//                }
            }

//            if (videoWatchData.isNotEmpty()) {
//                val courseNetwork = CourseNetwork(context)
//                courseNetwork.syncVideoWatchData(videoWatchData)
//                        .enqueue(object : TestpressCallback<Void>(){
//                            override fun onSuccess(result: Void?) {
//                                dao.deleteAll()
//                            }
//
//                            override fun onException(exception: TestpressException?) {}
//                        })
//            }
        }

    }
}