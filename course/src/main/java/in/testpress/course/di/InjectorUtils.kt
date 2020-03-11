package `in`.testpress.course.di

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.repository.ContentRepository
import `in`.testpress.course.repository.ExamContentRepository
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.exam.network.ExamNetwork
import android.content.Context

object InjectorUtils {
    fun getContentRepository(context: Context): ContentRepository {
        val contentDao = TestpressSDKDatabase.getContentDao(context)
        val attachmentDao = TestpressSDKDatabase.getAttachmentDao(context)
        val htmlContentDao = TestpressSDKDatabase.getHtmlContentDao(context)
        val videoContentDao = TestpressSDKDatabase.getVideoDao(context)
        val examDao = TestpressSDKDatabase.getExamDao(context)
        val roomContentDao = TestpressDatabase(context).contentDao()
        val roomAttachmentDao = TestpressDatabase(context).attachmentDao()
        val roomExamDao = TestpressDatabase(context).examDao()
        val roomHtmlDao = TestpressDatabase(context).htmlDao()
        val roomVideoDao = TestpressDatabase(context).videoDao()
        return ContentRepository(
            roomContentDao,
            contentDao,
            attachmentDao,
            htmlContentDao,
            videoContentDao,
            examDao,
            getCourseNetwork(context),
            roomAttachmentDao,
            roomHtmlDao,
            roomVideoDao,
            roomExamDao
            )
    }

    fun getExamRepository(context: Context): ExamContentRepository {
        val attemptDao = TestpressSDKDatabase.getAttemptDao(context)
        val contentAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(context)
        val languageDao = TestpressSDKDatabase.getLanguageDao(context)
        val roomAttemptDao = TestpressDatabase(context).attemptDao()
        val roomContentAttemptDao = TestpressDatabase(context).contentAttemptDao()
        val roomLanguageDao = TestpressDatabase(context).languageDao()

        return ExamContentRepository(
            getCourseNetwork(context),
            getExamNetwork(context),
            contentAttemptDao,
            attemptDao,
            languageDao,
            roomContentAttemptDao,
            roomAttemptDao,
            roomLanguageDao
        )
    }

    private fun getExamNetwork(context: Context): ExamNetwork {
        return ExamNetwork(context)
    }

    private fun getCourseNetwork(context: Context): CourseNetwork {
        return CourseNetwork(context)
    }
}