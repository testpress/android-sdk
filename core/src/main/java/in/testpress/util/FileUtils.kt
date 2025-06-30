package `in`.testpress.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
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

fun deleteFileFromPath(filePath: String) {
    try {
        val file = File(Uri.parse(filePath).path ?: return)
        if (file.exists()) {
            val deleted = file.delete()
            Log.d("AttachmentDelete", "File deleted: $deleted")
        } else {
            Log.d("AttachmentDelete", "File does not exist at $filePath")
        }
    } catch (e: Exception) {
        Log.e("AttachmentDelete", "Error deleting file: ${e.message}", e)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun deleteFileFromContentUri(context: Context, contentUri: String) {
    try {
        val rowsDeleted = context.contentResolver.delete(contentUri.toUri(), null, null)
        Log.d("AttachmentDelete", "Deleted $rowsDeleted rows for $contentUri")
    } catch (e: Exception) {
        Log.e("AttachmentDelete", "Failed to delete content URI: $contentUri", e)
    }
}

fun openFile(context: Context, filePath: String?, contentUri: String?, mimeType: String) {
    try {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !contentUri.isNullOrBlank()) {
            contentUri.toUri()
        } else if (!filePath.isNullOrBlank()) {
            val file = File(Uri.parse(filePath).path ?: return)
            FileProvider.getUriForFile(context, "${context.packageName}.testpressFileProvider", file)
        } else {
            Log.e("AttachmentOpen", "No valid path or URI to open file")
            return
        }

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(openIntent)

    } catch (e: ActivityNotFoundException) {
        Log.e("AttachmentOpen", "No app found to open file: ${e.message}")
    } catch (e: Exception) {
        Log.e("AttachmentOpen", "Failed to open file: ${e.message}", e)
    }
}

fun getMimeTypeFromUri(context: Context, filePath: String?, contentUri: String?): String {
    return try {
        when {
            // For Android Q+ Content URIs
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !contentUri.isNullOrBlank() -> {
                val uri = Uri.parse(contentUri)
                context.contentResolver.getType(uri) ?: "*/*"
            }

            // For file paths
            !filePath.isNullOrBlank() -> {
                val file = File(Uri.parse(filePath).path ?: return "*/*")
                val extension = MimeTypeMap.getFileExtensionFromUrl(file.name)
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "*/*"
            }

            else -> "*/*"
        }
    } catch (e: Exception) {
        "*/*"
    }
}

