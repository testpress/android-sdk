package `in`.testpress.course.util

import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.InstituteSettings
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.util.Date

class CourseApplication : Application() {
    private lateinit var downloadDirectory: File
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("DATE", Context.MODE_PRIVATE)
        storeTodayDate()
    }

    private fun storeTodayDate() {
        val today = Date()
        if (isDeviceTimeCorrect()) {
            sharedPreferences.edit().putLong("TODAY", today.time).apply()
        }
    }

    private fun isDeviceTimeCorrect(): Boolean {
        val instituteSettings: InstituteSettings = TestpressSdk.getTestpressSession(this)!!.instituteSettings
        val oldDate = sharedPreferences.getLong("TODAY", -1)
        val today = Date()
        return today.time > oldDate && today.time > instituteSettings.serverTime
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