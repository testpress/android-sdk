package `in`.testpress.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.provider.OpenableColumns

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