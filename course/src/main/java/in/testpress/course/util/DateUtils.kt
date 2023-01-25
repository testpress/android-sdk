package `in`.testpress.course.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.text.DateFormat
import java.text.SimpleDateFormat
import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.Direction.NEXT
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit.*
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    const val ONE_DAY_IN_MILLI_SECONDS = 1000 * 60 * 60 * 24

    private val ONE_SECOND_IN_MILLISECOND = 1000L
    private val MONTHS_IN_YEAR = 12L
    private val DAYS_IN_CURRENT_MONTH = LocalDate.now().lengthOfMonth()
    private val ONE_MINUTES_IN_SECONDS = 60L
    private val ONE_HOURS_IN_SECONDS = 60L * ONE_MINUTES_IN_SECONDS
    private val ONE_DAY_IN_SECONDS = 24L * ONE_HOURS_IN_SECONDS
    private val ONE_MONTH_IN_SECONDS = DAYS_IN_CURRENT_MONTH * ONE_DAY_IN_SECONDS
    private val ONE_YEAR_IN_SECONDS = 12L * ONE_MONTH_IN_SECONDS

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

    fun getHumanizedDateFormat(startTimeOrEndTime: String?):String? {
        val currentDate = Date()
        val startOrEndDate = convertStringToDate(startTimeOrEndTime)
        val secondDifference = getDateDifferentInSecond(startOrEndDate,currentDate)
        return getStringAccordingToGivenSeconds(secondDifference)
    }

    private fun convertStringToDate(dateString: String?): Date? {
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

    private fun getDateDifferentInSecond(date1: Date?, date2: Date?): Long? {
        if (date1 == null || date2 == null) {
            return null
        }
        val diff: Long = date1.time - date2.time
        return diff / ONE_SECOND_IN_MILLISECOND
    }

    private fun getStringAccordingToGivenSeconds(second: Long?): String? {
        val fmt: RelativeDateTimeFormatter = RelativeDateTimeFormatter.getInstance()
        return when {
            second == null -> null
            second > ONE_YEAR_IN_SECONDS -> {  // output in 1 year
                fmt.format(
                    ((TimeUnit.SECONDS.toDays(second) / DAYS_IN_CURRENT_MONTH) / MONTHS_IN_YEAR).toDouble(),
                    NEXT,
                    YEARS
                )
            }
            second > ONE_MONTH_IN_SECONDS -> {  // output in 2 months
                fmt.format(
                    (TimeUnit.SECONDS.toDays(second) / DAYS_IN_CURRENT_MONTH).toDouble(),
                    NEXT,
                    MONTHS
                )
            }
            second > ONE_DAY_IN_SECONDS -> {  // output in 5 days
                fmt.format(TimeUnit.SECONDS.toDays(second).toDouble(), NEXT, DAYS)
            }
            second > ONE_HOURS_IN_SECONDS -> {  // output in 10 hours
                fmt.format(TimeUnit.SECONDS.toHours(second).toDouble(), NEXT, HOURS)
            }
            else -> {  // output in 10 minutes
                fmt.format(TimeUnit.SECONDS.toMinutes(second).toDouble(), NEXT, MINUTES)
            }
        }
    }
}