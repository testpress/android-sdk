package `in`.testpress.util

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment.*
import androidx.core.content.getSystemService

class FileDownloader(private val context: Context) {

    fun downloadFile(fileUrl: String, fileName: String) {
        val request = getDownloadManagerRequest(fileUrl,fileName)
        val downloadManager = context.getSystemService<DownloadManager>()
        downloadManager?.enqueue(request) ?: return
    }

    private fun getDownloadManagerRequest(fileUrl: String, fileName: String): DownloadManager.Request{
        return DownloadManager.Request(Uri.parse(fileUrl)).apply {
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            setAllowedOverRoaming(true)
            setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, fileName)
        }
    }
}

enum class FileType(val extension: String) {
    PDF(".pdf")
}