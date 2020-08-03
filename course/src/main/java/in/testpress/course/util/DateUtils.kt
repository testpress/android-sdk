package `in`.testpress.course.util

import java.util.Date

object DateUtils {
    const val ONE_DAY_IN_MILLI_SECONDS = 1000 * 60 * 60 * 24

    fun difference(start: Date, end: Date): Long {
        val diffTime = end.time - start.time
        return diffTime / ONE_DAY_IN_MILLI_SECONDS
    }
}