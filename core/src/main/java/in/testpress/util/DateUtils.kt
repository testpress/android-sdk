package `in`.testpress.util

import java.util.Date

object DateUtils {
    fun difference(start: Date, end: Date): Long {
        val diffTime = end.time - start.time
        return diffTime / (1000 * 60 * 60 * 24)
    }
}