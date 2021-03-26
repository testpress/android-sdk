package `in`.testpress.util

object TimeUtils {
    @JvmStatic
    fun convertMilliSecondsToSeconds(milliseconds: Long): Long {
        return maxOf(0, milliseconds) / 1000
    }
}
