package `in`.testpress.course.helpers

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.util.CourseApplication
import `in`.testpress.course.util.DateUtils
import android.content.Context
import java.util.Date

class CourseRefreshDate(val context: Context) {
    private val sharedPreferences = context.getSharedPreferences(COURSE_DATA, Context.MODE_PRIVATE)
    private val application = context.applicationContext as CourseApplication
    private val instituteSettings = TestpressSdk.getTestpressSession(context)!!.instituteSettings

    fun hasNotUpdated(): Boolean {
        val lastFetchTime = sharedPreferences.getLong(COURSE_REFRESH_TIME, 0)
        val now = Date()
        return DateUtils.difference(Date(lastFetchTime), now) > 15 && now.time > instituteSettings.serverTime
    }

    fun update() {
        val today = Date()
        if (!application.isAutoTimeDisabledInDevice()) {
            sharedPreferences.edit().putLong(COURSE_REFRESH_TIME, today.time).apply()
        }
    }

    companion object {
        const val COURSE_DATA = "courseData"
        const val COURSE_REFRESH_TIME = "courseRefreshTime"
    }
}