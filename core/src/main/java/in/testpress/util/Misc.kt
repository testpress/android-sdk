package `in`.testpress.util

import android.content.Context
import android.net.ConnectivityManager
import java.net.URL
import java.util.*

object Misc {
    fun hasNetworkAvailable(context: Context): Boolean {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager?
        val network = manager?.activeNetworkInfo
        return (network != null)
    }

    fun addDaysToMilliSeconds(milliSeconds: Long, noOfDays: Int): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = milliSeconds
        c.add(Calendar.DAY_OF_MONTH, noOfDays)
        return c.timeInMillis
    }

    fun getPathFromURL(url: String) {
        URL(url).path.replaceFirst("/", "")
    }
}