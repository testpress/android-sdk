package `in`.testpress.course.util

import android.app.Application
import android.content.Context
import java.io.File
import java.util.Date

class CourseApplication : Application() {
    private lateinit var downloadDirectory: File

    override fun onCreate() {
        super.onCreate()
        storeTodayDate()
    }

    private fun storeTodayDate() {
        val today = Date()
        val sharedPreferences = getSharedPreferences("DATE", Context.MODE_PRIVATE)
        val oldDate = sharedPreferences.getLong("TODAY", -1)
        if (oldDate < today.time) {
            sharedPreferences.edit().putLong("TODAY", today.time).apply()
        }
    }

    fun getCurrentDateTime(): Long {
        val sharedPreferences = getSharedPreferences("DATE", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("TODAY", -1)
    }

    fun getDownloadDirectory(): File {
        if (!::downloadDirectory.isInitialized) {
            downloadDirectory = if (getExternalFilesDir(null) != null) {
                getExternalFilesDir(null)!!
            } else {
                filesDir
            }
        }
        return downloadDirectory
    }
}