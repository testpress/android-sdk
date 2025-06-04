package `in`.testpress.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun copyFileFromUriAndUpload(
    context: Context,
    uri: Uri?,
    onSuccess: (filePath: String) -> Unit,
    onError: (error: String) -> Unit
) {
    if (uri == null) onError("No file selected for upload")
    var inputStream: InputStream? = null
    var outputStream: FileOutputStream? = null

    try {
        inputStream = context.contentResolver.openInputStream(uri!!)
        if (inputStream == null) {
            onError("Unable to read selected file")
            return
        }

        val tempFile = File(context.cacheDir, "upload_temp.pdf")
        outputStream = FileOutputStream(tempFile)

        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        onSuccess(tempFile.absolutePath)

    } catch (e: Exception) {
        e.printStackTrace()
        onError("Failed to copy and upload file: ${e.message}")
    } finally {
        inputStream?.close()
        outputStream?.close()
    }
}