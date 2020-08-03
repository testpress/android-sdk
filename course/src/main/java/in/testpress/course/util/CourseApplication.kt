package `in`.testpress.course.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import java.io.File

class CourseApplication : Application() {
    private lateinit var downloadDirectory: File

    fun isAutoTimeDisabledInDevice(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.Global.getInt(this.contentResolver, Settings.Global.AUTO_TIME, 0) != 1;
        } else {
            Settings.System.getInt(this.contentResolver, Settings.System.AUTO_TIME, 0) != 1;
        }
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