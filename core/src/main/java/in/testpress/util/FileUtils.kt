package `in`.testpress.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.net.URLDecoder
import java.util.*

fun copyFileFromUriAndUpload(
    context: Context,
    uri: Uri?,
    onSuccess: (filePath: String) -> Unit,
    onError: (error: String) -> Unit
) {
    if (uri == null) {
        onError("No file selected for upload")
        return
    }

    var inputStream: InputStream? = null
    var outputStream: FileOutputStream? = null
    var tempFile: File? = null

    try {
        inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            onError("Unable to read selected file")
            return
        }

        val fileName = getFileNameFromUri(context, uri) ?: "upload_temp"
        tempFile = File(context.cacheDir, fileName)

        outputStream = FileOutputStream(tempFile)

        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        outputStream.flush()

    } catch (e: Exception) {
        e.printStackTrace()
        tempFile?.delete()
        onError("Failed to copy and upload file: ${e.message}")
        return
    } finally {
        try {
            outputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    tempFile?.absolutePath?.let {
        onSuccess(it)
    } ?: run {
        onError("Temporary file creation failed")
    }
}


private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1) {
                name = it.getString(index)
            }
        }
    }
    return name
}

fun deleteFileByPath(filePath: String): Boolean {
    return try {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

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

    if (!file.exists()) {
        Toast.makeText(context, "File not found.", Toast.LENGTH_SHORT).show()
        return
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.testpressFileProvider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri))
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this file type.", Toast.LENGTH_SHORT).show()
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

fun getMimeTypeFromUrl(url: String): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: ""
}