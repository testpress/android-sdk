package `in`.testpress.course.helpers

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.util.DateUtils
import android.content.Context
import java.util.Date

class CourseLastSyncedDate(val context: Context) {
    private val sharedPreferences = context.getSharedPreferences(COURSE_DATA, Context.MODE_PRIVATE)
    private val instituteSettings = TestpressSdk.getTestpressSession(context)!!.instituteSettings

    fun hasExpired(): Boolean {
        val lastFetchTime = sharedPreferences.getLong(COURSE_REFRESH_TIME, 0)
        val now = Date()
        return DateUtils.difference(Date(lastFetchTime), now) > 2 && now.time > instituteSettings.serverTime
    }

    fun refresh() {
        val today = Date()
        if (!DateUtils.isAutoTimeUpdateDisabledInDevice(context)) {
            sharedPreferences.edit().putLong(COURSE_REFRESH_TIME, today.time).apply()
        }
    }

    companion object {
        const val COURSE_DATA = "courseData"
        const val COURSE_REFRESH_TIME = "courseRefreshTime"
    }
}