package `in`.testpress.course.helpers

import `in`.testpress.course.util.CourseApplication
import `in`.testpress.course.util.DateUtils
import android.content.Context
import java.util.Date

class CourseRefreshDate(val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("COURSES_FETCH", Context.MODE_PRIVATE)

    private fun isDeviceTimeCorrect(): Boolean {
        val courseApplication = context.applicationContext as CourseApplication
        return courseApplication.isDeviceTimeCorrect()
    }

    fun hasNotUpdated(): Boolean {
        val lastFetchTime = sharedPreferences.getLong("FETCH_TIME", 0)
        val today = Date()
        return DateUtils.difference(Date(lastFetchTime), today) > 15 && isDeviceTimeCorrect()
    }

    fun update() {
        val today = Date()
        if (isDeviceTimeCorrect()) {
            sharedPreferences.edit().putLong("FETCH_TIME", today.time).apply()
        }
    }
}