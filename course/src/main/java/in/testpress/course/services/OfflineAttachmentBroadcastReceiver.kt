package `in`.testpress.course.services

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineAttachmentBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return

        val dao = TestpressDatabase.invoke(context).offlineAttachmentDao()
        val repository = OfflineAttachmentsRepository(dao)
        val downloadManager = context.getSystemService(DownloadManager::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val attachment = repository.getByDownloadId(downloadId)
            if (attachment != null) {

                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                var path: String? = null
                var contentUri: String? = null

                cursor?.use {
                    if (it.moveToFirst()) {
                        val localUri = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                        val mediaProviderUri = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIAPROVIDER_URI))
                        path = localUri
                        contentUri = mediaProviderUri
                    }
                }

                repository.update(
                    attachment.copy(
                        path = path ?: attachment.path,
                        contentUri = contentUri ?: attachment.contentUri,
                        status = OfflineAttachmentDownloadStatus.COMPLETED,
                        progress = 100
                    )
                )
            }
        }
    }
}
