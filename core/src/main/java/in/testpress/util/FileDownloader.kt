package `in`.testpress.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment.*
import androidx.core.content.getSystemService

class FileDownloader(private val context: Context) {

    fun downloadFile(fileUrl: String, fileName: String) {
        val request = getDownloadManagerRequest(fileUrl,fileName.sanitizeFileName())
        val downloadManager = context.getSystemService<DownloadManager>()
        downloadManager?.enqueue(request) ?: return
        ViewUtils.toast(context,"Download Started...")
    }

    private fun getDownloadManagerRequest(fileUrl: String, fileName: String): DownloadManager.Request{
        return DownloadManager.Request(Uri.parse(fileUrl)).apply {
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverRoaming(true)
            setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, fileName)
        }
    }
}

enum class FileType(val extension: String) {
    PDF(".pdf")
}