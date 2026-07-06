package `in`.testpress.course.services

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import io.sentry.Breadcrumb
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
        val downloadManager = context.getSystemService(DownloadManager::class.java) ?: return

        val attachment = repository.getByDownloadId(downloadId)
        if (attachment == null) {
            // Race condition: broadcast fired before DB insert completed
            Sentry.captureMessage(
                "BroadcastReceiver: No DB record for downloadId=$downloadId. " +
                "Race condition — broadcast fired before DB insert completed."
            )
            return
        }
        val downloadInfo = queryDownloadInfo(downloadManager, downloadId)

        Sentry.addBreadcrumb(Breadcrumb().apply {
            category = "download"
            message = "Broadcast received"
            data["downloadId"] = downloadId
            data["attachmentId"] = attachment.id
            data["status"] = if (downloadInfo.status == DownloadManager.STATUS_SUCCESSFUL) "SUCCESSFUL" else "FAILED"
            if (downloadInfo.status == DownloadManager.STATUS_FAILED) {
                data["failureReason"] = downloadInfo.reason.toString()
            }
        })

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
                Sentry.captureMessage(
                    "BroadcastReceiver: Download FAILED for attachmentId=${attachment.id}, " +
                    "downloadId=$downloadId, reason=${downloadInfo.reason}"
                )
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
        var reason = 0

        cursor?.use {
            if (it.moveToFirst()) {
                status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    path = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    contentUri =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIAPROVIDER_URI))
                        } else {
                            null
                        }
                }
            }
        }
        return DownloadInfo(path, contentUri, status, reason)
    }

    private data class DownloadInfo(
        val path: String?,
        val contentUri: String?,
        val status: Int,
        val reason: Int = 0
    )
}
