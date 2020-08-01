package `in`.testpress.course.util

import `in`.testpress.core.TestpressSdk
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
        sharedPreferences = getSharedPreferences(APP_DATA, Context.MODE_PRIVATE)
        storeAppStartTime()
    }

    private fun storeAppStartTime() {
        val today = Date()
        if (isDeviceTimeCorrect()) {
            sharedPreferences.edit().putLong(APP_TIME, today.time).apply()
        }
    }

    fun isDeviceTimeCorrect(): Boolean {
        val instituteSettings = TestpressSdk.getTestpressSession(this)!!.instituteSettings
        val previousAppTime = sharedPreferences.getLong(APP_TIME, -1)
        val now = Date()
        return now.time > previousAppTime && now.time > instituteSettings.serverTime
    }

    fun isDeviceTimeInCorrect(): Boolean {
        return !isDeviceTimeCorrect()
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

    companion object {
        const val APP_TIME = "appStartTime"
        const val APP_DATA = "appData"
    }
}