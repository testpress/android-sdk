package `in`.testpress.course.services

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.util.*
import io.sentry.Sentry
import kotlinx.coroutines.*

class OfflineAttachmentDownloadManager private constructor(private val repository: OfflineAttachmentsRepository) {

    private val progressScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeJobs = mutableMapOf<Long, Job>()

    fun enqueueDownload(context: Context, domainAttachmentContent: DomainAttachmentContent) {
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        if (downloadManager == null) {
            Toast.makeText(context, "DownloadManager not available", Toast.LENGTH_SHORT).show()
            return
        }

        if (domainAttachmentContent.attachmentUrl == null) {
            Toast.makeText(context, "Attachment URL cannot be null", Toast.LENGTH_SHORT).show()
            return
        }
        val fileName =
            "${domainAttachmentContent.title}${getFileExtensionFromUrl(domainAttachmentContent.attachmentUrl)}"
        val request = getDownloadManagerRequest(domainAttachmentContent.attachmentUrl, fileName)

        val downloadId = downloadManager.enqueue(request)

        val offlineAttachment = OfflineAttachment(
            id = domainAttachmentContent.id,
            title = domainAttachmentContent.title ?: "Attachment ${domainAttachmentContent.id}",
            url = domainAttachmentContent.attachmentUrl,
            path = "",
            contentUri = null,
            downloadId = downloadId,
            status = OfflineAttachmentDownloadStatus.QUEUED,
            progress = 0
        )

        progressScope.launch {
            repository.insert(offlineAttachment)
        }

        trackDownloadProgress(context, downloadId)
    }

    private fun getDownloadManagerRequest(
        fileUrl: String,
        fileName: String
    ): DownloadManager.Request {
        return DownloadManager.Request(fileUrl.let(Uri::parse)).apply {
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
    }

    private fun trackDownloadProgress(context: Context, downloadId: Long) {
        if (activeJobs.containsKey(downloadId)) return

        val job = progressScope.launch {
            val downloadManager = context.getSystemService(DownloadManager::class.java)
                ?: return@launch
            val query = DownloadManager.Query().setFilterById(downloadId)

            var lastProgress = -1
            var lastStatus: Int? = null

            while (isActive) {
                val isFinished = try {
                    val cursor = downloadManager.query(query)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val status =
                                it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                            val totalSize =
                                it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                            val downloadedSize =
                                it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val localUri =
                                it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))

                            val progress = calculateProgress(downloadedSize, totalSize)

                            handleStatusChange(downloadId, status, localUri, lastStatus)
                                .also { lastStatus = status }

                            handleProgressChange(downloadId, progress, lastProgress)
                                .also { lastProgress = progress }

                            isDownloadFinished(status)
                        } else true
                    } ?: true
                } catch (e: SecurityException) {
                    Sentry.captureException(e)
                    Log.e(
                        "DownloadTracker",
                        "SecurityException querying DownloadManager for downloadId: $downloadId",
                        e
                    )
                    true
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    Log.e(
                        "DownloadTracker",
                        "Unexpected exception querying DownloadManager for downloadId: $downloadId",
                        e
                    )
                    true
                }

                if (isFinished) break
                delay(1000)
            }
            activeJobs.remove(downloadId)
        }

        activeJobs[downloadId] = job
    }

    private fun calculateProgress(downloaded: Long, total: Long): Int {
        return if (total > 0) ((downloaded * 100) / total).toInt() else 0
    }

    private fun isDownloadFinished(status: Int): Boolean {
        return status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED
    }

    private suspend fun handleStatusChange(
        downloadId: Long,
        currentStatus: Int,
        localUri: String?,
        lastStatus: Int?
    ) {
        if (currentStatus != lastStatus) {
            when (currentStatus) {
                DownloadManager.STATUS_PENDING -> {
                    repository.updateStatusWithDownloadId(downloadId, OfflineAttachmentDownloadStatus.QUEUED)
                }

                DownloadManager.STATUS_RUNNING -> {
                    localUri?.let {
                        repository.updateFilePathWithDownloadId(downloadId, it)
                    }
                    repository.updateStatusWithDownloadId(downloadId, OfflineAttachmentDownloadStatus.DOWNLOADING)
                }

                DownloadManager.STATUS_FAILED -> {
                    repository.updateStatusWithDownloadId(downloadId, OfflineAttachmentDownloadStatus.FAILED)
                }
            }
        }
    }

    private suspend fun handleProgressChange(downloadId: Long, currentProgress: Int, lastProgress: Int) {
        if (currentProgress != lastProgress) {
            repository.updateProgressWithDownloadId(downloadId, currentProgress)
        }
    }

    fun cancelDownload(context: Context, offlineAttachment: OfflineAttachment) {
        val downloadManager = context.getSystemService(DownloadManager::class.java)
            ?: return

        downloadManager.remove(offlineAttachment.downloadId)

        activeJobs[offlineAttachment.downloadId]?.cancel()
        activeJobs.remove(offlineAttachment.downloadId)

        progressScope.launch {
            repository.delete(offlineAttachment.id)
        }
    }

    fun deleteDownload(context: Context, offlineAttachment: OfflineAttachment) {
        progressScope.launch {
            repository.delete(offlineAttachment.id)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                offlineAttachment.contentUri?.let {
                    deleteFileFromContentUri(context, it)
                }
            } else {
                deleteFileFromPath(offlineAttachment.path)
            }
        }
    }

    fun openFiles(context: Context, offlineAttachment: OfflineAttachment) {
        val filePathUri = offlineAttachment.path
        val contentUri = offlineAttachment.contentUri
        val mimeType = getMimeTypeFromUri(context, filePathUri, contentUri)
        openFile(context, filePathUri, contentUri, mimeType)
    }

    fun restartDownloadProgressTracking(context: Context) {
        progressScope.launch {
            val downloadingAttachments =
                repository.getAllWithStatus(OfflineAttachmentDownloadStatus.DOWNLOADING)
            val queuedAttachments =
                repository.getAllWithStatus(OfflineAttachmentDownloadStatus.QUEUED)

            val attachmentsToTrack = downloadingAttachments + queuedAttachments

            attachmentsToTrack.forEach { attachment ->
                if (attachment.downloadId > 0) {
                    trackDownloadProgress(context, attachment.downloadId)
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: OfflineAttachmentDownloadManager? = null

        fun init(repository: OfflineAttachmentsRepository) {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = OfflineAttachmentDownloadManager(repository)
                }
            }
        }

        fun getInstance(): OfflineAttachmentDownloadManager {
            return INSTANCE ?: throw IllegalStateException(
                "OfflineAttachmentDownloadManager is not initialized. Call init() in your Application class."
            )
        }
    }
}
