package `in`.testpress.course.repository

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.asGreenDaoModel
import android.content.Context

class VideoContentRepository(context: Context) : ContentRepository(context) {
    val videoDao = TestpressSDKDatabase.getVideoDao(context)

    override fun storeContentAndItsRelationsToDB(content: NetworkContent) {
        val greenDaoContent = content.asGreenDaoModel()
        content.video?.let {
            val video = it.asGreenDaoModel()
            greenDaoContent.videoId = video.id
            videoDao.insertOrReplace(it.asGreenDaoModel())
        }
        contentDao.insertOrReplace(greenDaoContent)
    }
}