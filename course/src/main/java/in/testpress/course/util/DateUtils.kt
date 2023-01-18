package `in`.testpress.course.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    const val ONE_DAY_IN_MILLI_SECONDS = 1000 * 60 * 60 * 24

    fun difference(start: Date, end: Date): Long {
        val diffTime = end.time - start.time
        return diffTime / ONE_DAY_IN_MILLI_SECONDS
    }

    fun isAutoTimeUpdateDisabledInDevice(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME, 0) != 1;
        } else {
            Settings.System.getInt(context.contentResolver, Settings.System.AUTO_TIME, 0) != 1;
        }
    }

    fun convertDurationStringToSeconds(durationString: String): Int {
        val durationList = durationString.split(":").toMutableList()
        var seconds = 0
        var minutes = 1

        while (durationList.size > 0) {
            seconds += minutes * durationList.removeLast().toInt()
            minutes *= 60
        }

        return seconds
    }

    fun getFormattedStartDateAndEndDate(start: String?,end: String?): String {
        var startAndEnd = ""
        if (getFormattedStartDate(start) != ""){
            startAndEnd += "Start: ${getFormattedStartDate(start)}"  //Result should be like [Start: 01/01/23 10:00 am]
        }
        if (getFormattedEndDate(end) != ""){
            startAndEnd += if (startAndEnd == ""){
                "End: ${getFormattedEndDate(end)}"  //Result should be like [End: 01/01/23 12:00 pm]
            } else {
                " - End: ${getFormattedEndDate(end)}" //Result should be like [Start: 01/01/23 10:00 am - End: 01/01/23 12:00 pm]
            }
        }
        return startAndEnd
    }

    fun getFormattedStartDate(start: String?): String {
        var startDateAndTime = ""
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        if (start != null && start != "") {
            startDateAndTime = try {
                val date = start.let { simpleDateFormat.parse(it) }
                val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                date?.let { dateFormat.format(it) }!!
            } catch (e: Exception) {
                ""
            }
        }
        return startDateAndTime  // 01/01/23 10:00 am
    }

    fun getFormattedEndDate(end: String?): String {
        var endDateAndTime = ""
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        if (end != null && end != "") {
            endDateAndTime = try {
                val date = end.let { simpleDateFormat.parse(it) }
                val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                date?.let { dateFormat.format(it) }!!
            } catch (e: Exception) {
                ""
            }
        }
        return endDateAndTime  // 01/01/23 10:00 am
    }
}