package `in`.testpress.course.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLDecoder
import java.util.Locale

object FileUtils {
    fun getRootDirPath(context: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file: File = ContextCompat.getExternalFilesDirs(
                    context.applicationContext,
                    null
            )[0]
            file.absolutePath
        } else {
            context.applicationContext.filesDir.absolutePath
        }
    }

    fun openFile(context: Context, path: String) {
        val file = File(path)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.testpressFileProvider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, null)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app found to open this file type.", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFile(path: String) {
        val file = File(path)
        if (file.exists()) {
            try {
                file.delete()
            } catch (e: Exception) {
                Log.e("AttachmentDelete", "Failed to delete file: ${file.absolutePath}", e)
            }
        }
    }

    fun getFileExtensionFromUrl(url: String?): String {
        if (url.isNullOrEmpty()) return ".pdf"

        val uri = Uri.parse(url)

        // Try to get filename from response-content-disposition param
        val filename = uri.getQueryParameter("response-content-disposition")?.let { disposition ->
            val decoded = URLDecoder.decode(disposition, "UTF-8")
            val index = decoded.indexOf("filename=")
            if (index != -1) {
                decoded.substring(index + 9).trim('"', ' ', ';')
            } else null
        }

        // Extract extension from filename if available
        filename?.let {
            val dotIndex = it.lastIndexOf('.')
            if (dotIndex != -1 && dotIndex < it.length - 1) {
                return "." + it.substring(dotIndex + 1).lowercase(Locale.getDefault())
            }
        }

        // Fallback: Extract extension from URL path
        uri.path?.let { path ->
            val dotIndex = path.lastIndexOf('.')
            if (dotIndex != -1 && dotIndex < path.length - 1) {
                return "." + path.substring(dotIndex + 1).lowercase(Locale.getDefault())
            }
        }

        return ".pdf"
    }
}
