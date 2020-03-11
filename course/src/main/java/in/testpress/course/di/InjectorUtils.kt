package `in`.testpress.course.di

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.repository.ContentRepositoryFactory
import `in`.testpress.database.TestpressDatabase
import android.content.Context

object InjectorUtils {
    fun getContentRepository(contentType: String, context: Context): ContentRepository {
        return ContentRepositoryFactory.getRepository(contentType, context)
    }

    private fun getCourseNetwork(context: Context): CourseNetwork {
        return CourseNetwork(context)
    }
}