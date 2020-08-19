package `in`.testpress.course.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.Date

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
}