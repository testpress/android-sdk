package `in`.testpress.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import java.net.URL
import java.util.*
import android.webkit.MimeTypeMap




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

    fun getMimeType(url: String): String {
        var type = "multipart/form-data"
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension).toString()
        }
        return type
    }

    fun isAndroid13OrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}