package `in`.testpress.util

object TimeUtils {
    @JvmStatic
    fun convertMilliSecondsToSeconds(milliseconds: Long): Float {
        return maxOf(0, milliseconds).toFloat() / 1000
    }
}