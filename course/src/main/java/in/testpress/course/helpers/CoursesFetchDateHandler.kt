package `in`.testpress.course.helpers

import `in`.testpress.course.util.CourseApplication
import `in`.testpress.course.util.DateUtils
import android.content.Context
import java.util.Date

class CoursesFetchDateHandler(val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("COURSES_FETCH", Context.MODE_PRIVATE)

    fun isDateTampered(): Boolean {
        val today = Date()
        val courseApplication = context.applicationContext as CourseApplication
        return courseApplication.getCurrentDateTime() > today.time
    }

    fun hasNotUpdated(): Boolean {
        val lastFetchTime = sharedPreferences.getLong("FETCH_TIME", 0)
        val today = Date()

        if (isDateTampered()) {
            return true
        }
        return DateUtils.difference(Date(lastFetchTime), today) > 15
    }

    fun update() {
        val today = Date()
        sharedPreferences.edit().putLong("FETCH_TIME", today.time).apply()
    }
}