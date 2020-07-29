package `in`.testpress.course.util

import android.app.Application
import java.io.File

class CourseApplication : Application() {
    private lateinit var downloadDirectory: File

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