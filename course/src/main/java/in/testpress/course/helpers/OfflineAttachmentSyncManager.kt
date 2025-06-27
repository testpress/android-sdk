package `in`.testpress.course.helpers

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import `in`.testpress.database.dao.OfflineAttachmentsDao
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OfflineAttachmentSyncManager(
    private val context: Context,
    private val dao: OfflineAttachmentsDao
) {

    suspend fun syncDownloads() = withContext(Dispatchers.IO) {
        val attachments = dao.getAll()
        val resolver = context.contentResolver

        attachments.forEach { attachment ->
            try {
                val fileExists = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val uri = attachment.contentUri?.let(Uri::parse)
                    uri != null && runCatching {
                        resolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
                    }.getOrDefault(false)
                } else {
                    val file = File(attachment.path)
                    file.exists()
                }

                if (!fileExists && attachment.status == OfflineAttachmentDownloadStatus.COMPLETED) {
                    dao.updateStatus(attachment.id, OfflineAttachmentDownloadStatus.DELETE)
                    Log.i("AttachmentSync", "Marked deleted: ID=${attachment.id}")
                }

            } catch (e: Exception) {
                Log.e("AttachmentSync", "Error checking attachment ID=${attachment.id}", e)
            }
        }
    }
}
