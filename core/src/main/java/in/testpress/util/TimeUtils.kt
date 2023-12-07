package `in`.testpress.util

object TimeUtils {
    @JvmStatic
    fun convertMilliSecondsToSeconds(milliseconds: Long): Long {
        return maxOf(0, milliseconds) / 1000
    }

    fun convertDurationStringToSeconds(durationString: String?): Long {
        if (durationString == null) return 0
        val durationList = durationString.split(":").toMutableList()
        var seconds = 0L
        var minutes = 1L

        while (durationList.size > 0) {
            seconds += minutes * durationList.removeLast().toLong()
            minutes *= 60
        }

        return seconds
    }

    fun addTimeStrings(timeTaken: String?, remainingTime: String?): String {
        // Here, we add one second to totalTime because remainingTime is always one second less than the actual value.
        val totalTime: Long = convertDurationStringToSeconds(timeTaken) + convertDurationStringToSeconds(remainingTime) + 1
        val hours = (totalTime / (60 * 60)).toInt()
        val minutes = (totalTime / 60 % 60).toInt()
        val seconds = (totalTime % 60).toInt()
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
