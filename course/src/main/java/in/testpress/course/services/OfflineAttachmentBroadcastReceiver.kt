package `in`.testpress.course.services

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineAttachmentBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleDownloadCompletion(context, downloadId)
            } catch (e: Exception) {
                Sentry.captureException(e)
                Log.e("OfflineReceiver", "Error handling download completion", e)
            } finally {
                pendingResult.finish()
            }
        }

    }

    private suspend fun handleDownloadCompletion(context: Context, downloadId: Long) {
        val dao = TestpressDatabase.invoke(context).offlineAttachmentDao()
        val repository = OfflineAttachmentsRepository(dao)
        val downloadManager = context.getSystemService(DownloadManager::class.java)

        val attachment = repository.getByDownloadId(downloadId) ?: return
        val downloadInfo = queryDownloadInfo(downloadManager, downloadId)

        when (downloadInfo.status) {
            DownloadManager.STATUS_SUCCESSFUL -> {
                repository.update(
                    attachment.copy(
                        path = downloadInfo.path ?: attachment.path,
                        contentUri = downloadInfo.contentUri ?: attachment.contentUri,
                        status = OfflineAttachmentDownloadStatus.COMPLETED,
                        progress = 100
                    )
                )
            }

            DownloadManager.STATUS_FAILED -> {
                repository.update(
                    attachment.copy(
                        path = "",
                        contentUri = "",
                        status = OfflineAttachmentDownloadStatus.FAILED,
                        progress = 0
                    )
                )
            }
        }
    }

    private fun queryDownloadInfo(downloadManager: DownloadManager?, downloadId: Long): DownloadInfo {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager?.query(query)

        var path: String? = null
        var contentUri: String? = null
        var status = -1

        cursor?.use {
            if (it.moveToFirst()) {
                status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    path = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    contentUri = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIAPROVIDER_URI))
                }
            }
        }
        return DownloadInfo(path, contentUri, status)
    }

    private data class DownloadInfo(
        val path: String?,
        val contentUri: String?,
        val status: Int
    )
}
