package `in`.testpress.course.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import android.text.format.DateUtils.*
import `in`.testpress.course.R
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    const val YEAR = "Year"
    const val MONTH = "month"
    const val DAY = "day"
    const val HOUR = "hour"
    const val MINUTE = "minute"

    const val ONE_DAY_IN_MILLI_SECONDS = 1000 * 60 * 60 * 24

    private val CURRENT_MONTH_IN_MILLS get() = getCurrentMonthMills()
    private val CURRENT_YEAR_IN_MILLS get() = getCurrentYearMills()

    private fun getCurrentMonthMills(): Long {
        val calendar = Calendar.getInstance()
        val daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return TimeUnit.DAYS.toMillis(daysInCurrentMonth.toLong())
    }

    private fun getCurrentYearMills(): Long {
        val calendar = Calendar.getInstance()
        val daysInCurrentYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
        return TimeUnit.DAYS.toMillis(daysInCurrentYear.toLong())
    }

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

    fun getRelativeTimeString(startTimeOrEndTime: String?, context: Context): String {
        val futureMills = convertDateStringToMills(startTimeOrEndTime) ?: return ""
        val timeDifferenceInMills = futureMills - System.currentTimeMillis()
        val resource = context.resources
        return when {
            timeDifferenceInMills > CURRENT_YEAR_IN_MILLS -> {  // output -> in 1 year
                val yearCount = (timeDifferenceInMills / CURRENT_YEAR_IN_MILLS).toInt()
                resource.getQuantityString(R.plurals.time_duration, yearCount, yearCount, YEAR)
            }
            timeDifferenceInMills > CURRENT_MONTH_IN_MILLS -> {  // output -> in 2 months
                val monthCount = (timeDifferenceInMills / CURRENT_MONTH_IN_MILLS).toInt()
                resource.getQuantityString(R.plurals.time_duration, monthCount, monthCount, MONTH)
            }
            timeDifferenceInMills > DAY_IN_MILLIS -> {  // output -> in 5 days
                val daysCount = TimeUnit.MILLISECONDS.toDays(timeDifferenceInMills).toInt()
                resource.getQuantityString(R.plurals.time_duration, daysCount, daysCount, DAY)
            }
            timeDifferenceInMills > HOUR_IN_MILLIS -> {  // output -> in 10 hours
                val hoursCount = TimeUnit.MILLISECONDS.toHours(timeDifferenceInMills).toInt()
                resource.getQuantityString(R.plurals.time_duration, hoursCount, hoursCount, HOUR)
            }
            else -> {  // output -> in 10 minutes
                val minutesCount = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceInMills).toInt()
                resource.getQuantityString(R.plurals.time_duration, minutesCount, minutesCount, MINUTE)
            }
        }
    }

    private fun convertDateStringToMills(dateString: String?): Long? {
        if (dateString.isNullOrEmpty()) return null
        val regex = Regex(""".\d{6}""")
        val formattedDate = regex.replace(dateString, "")
        val simpleDateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        } else {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
        }
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val mills = try {
            formattedDate.let { simpleDateFormat.parse(it) }?.time
        } catch (e: Exception) {
            null
        }
        return mills
    }
}