package `in`.testpress.course.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    const val ONE_DAY_IN_MILLI_SECONDS = 1000 * 60 * 60 * 24
    const val ONE_YEAR_IN_HOUR = 8640
    const val ONE_MONTH_IN_HOUR = 720
    const val ONE_DAY_IN_HOUR = 24

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

    fun convertDateStringToDate(dateString: String?): Date? {
        var date: Date? = null
        val simpleDateFormat : SimpleDateFormat = if (dateString != null && dateString.length > 25){
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX") // Input String format "2023-01-23T18:38:57.345432+05:30"
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX") // Input String format "2023-01-23T18:38:57+05:30"
        }

        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        if (dateString != null && dateString != "") {
            date = try {
                dateString.let { simpleDateFormat.parse(it) }
            } catch (e: Exception) {
                null
            }
        }
        return date
    }

    fun getDateDifferentInHours(date1: Date?, date2: Date?): Long? {
        if (date1 == null || date2 == null) {
            return null
        }
        val diff: Long = date1.time - date2.time
        return ((diff / 1000L) / 60L) / 60L
    }
}