package `in`.testpress.course.di

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.database.TestpressDatabase
import android.content.Context

object InjectorUtils {
    fun getContentRepository(context: Context): ContentRepository {
        val contentDao = TestpressSDKDatabase.getContentDao(context)
        val attachmentDao = TestpressSDKDatabase.getAttachmentDao(context)
        val roomContentDao = TestpressDatabase(context).contentDao()
        return ContentRepository(roomContentDao, contentDao, attachmentDao, getCourseNetwork(context))
    }

    private fun getCourseNetwork(context: Context): CourseNetwork {
        return CourseNetwork(context)
    }
}