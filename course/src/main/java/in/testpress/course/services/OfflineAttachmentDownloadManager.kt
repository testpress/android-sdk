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
        // Log immediately to Sentry to verify connection and track user intent
        Sentry.captureMessage("Download initiated for attachmentId=${domainAttachmentContent.id} (${domainAttachmentContent.title})")

        val downloadManager = context.getSystemService(DownloadManager::class.java)
        if (downloadManager == null) {
            Toast.makeText(context, "DownloadManager not available", Toast.LENGTH_SHORT).show()
            Sentry.captureMessage("DownloadManager system service is null for attachmentId=${domainAttachmentContent.id}")
            return
        }

        if (domainAttachmentContent.attachmentUrl == null) {
            Toast.makeText(context, "Attachment URL cannot be null", Toast.LENGTH_SHORT).show()
            Sentry.captureMessage("Attachment URL is null for attachmentId=${domainAttachmentContent.id}")
            return
        }
        val fileName =
            "${domainAttachmentContent.title}${getFileExtensionFromUrl(domainAttachmentContent.attachmentUrl)}".sanitizeFileName()

        val downloadId = try {
            val request = getDownloadManagerRequest(domainAttachmentContent.attachmentUrl, fileName)
            downloadManager.enqueue(request)
        } catch (e: IllegalArgumentException) {
            Sentry.captureException(e) { scope ->
                scope.setExtra("attachmentId", domainAttachmentContent.id.toString())
                scope.setExtra("url", domainAttachmentContent.attachmentUrl ?: "")
            }
            Log.e("AttachDownload", "Invalid download request for attachmentId=${domainAttachmentContent.id}", e)
            Toast.makeText(context, "Cannot download: invalid file URL", Toast.LENGTH_SHORT).show()
            return
        }

        Sentry.addBreadcrumb(io.sentry.Breadcrumb().apply {
            category = "download"
            message = "Download enqueued"
            data["attachmentId"] = domainAttachmentContent.id
            data["downloadId"] = downloadId
            data["fileName"] = fileName
            data["url"] = domainAttachmentContent.attachmentUrl
        })

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
                            val reason =
                                it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))

                            val progress = calculateProgress(downloadedSize, totalSize)

                            handleStatusChange(downloadId, status, localUri, reason, lastStatus)
                                .also { lastStatus = status }

                            handleProgressChange(downloadId, progress, lastProgress)
                                .also { lastProgress = progress }

                            isDownloadFinished(status)
                        } else {
                            // DM entry gone — attachment is permanently stuck as QUEUED/DOWNLOADING
                            Sentry.captureMessage(
                                "DownloadManager entry disappeared for downloadId=$downloadId. " +
                                "DB record will be stuck. Last known status=${dmStatusName(lastStatus)}"
                            )
                            Sentry.addBreadcrumb(io.sentry.Breadcrumb().apply {
                                category = "download"
                                message = "DM entry disappeared — marking FAILED in DB"
                                data["downloadId"] = downloadId
                                data["lastStatus"] = dmStatusName(lastStatus)
                            })
                            repository.updateStatusWithDownloadId(downloadId, OfflineAttachmentDownloadStatus.FAILED)
                            true
                        }
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
        reason: Int,
        lastStatus: Int?
    ) {
        if (currentStatus != lastStatus) {
            Sentry.addBreadcrumb(io.sentry.Breadcrumb().apply {
                category = "download"
                message = "Status changed: ${dmStatusName(lastStatus)} → ${dmStatusName(currentStatus)}"
                data["downloadId"] = downloadId
                data["fromStatus"] = dmStatusName(lastStatus)
                data["toStatus"] = dmStatusName(currentStatus)
                if (currentStatus == DownloadManager.STATUS_FAILED) {
                    data["failureReason"] = dmErrorName(reason)
                }
            })

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
                    Sentry.captureMessage(
                        "Download FAILED for downloadId=$downloadId. " +
                        "Reason: ${dmErrorName(reason)} (code=$reason)"
                    )
                }
            }
        }
    }

    private suspend fun handleProgressChange(downloadId: Long, currentProgress: Int, lastProgress: Int) {
        if (currentProgress != lastProgress) {
            repository.updateProgressWithDownloadId(downloadId, currentProgress)
        }
    }

    /**
     * Maps DownloadManager status integers to human-readable names for Sentry.
     */
    private fun dmStatusName(status: Int?): String = when (status) {
        DownloadManager.STATUS_PENDING   -> "PENDING"
        DownloadManager.STATUS_RUNNING   -> "RUNNING"
        DownloadManager.STATUS_PAUSED    -> "PAUSED"
        DownloadManager.STATUS_SUCCESSFUL -> "SUCCESSFUL"
        DownloadManager.STATUS_FAILED    -> "FAILED"
        null                             -> "UNKNOWN(null)"
        else                             -> "UNKNOWN($status)"
    }

    /**
     * Maps DownloadManager COLUMN_REASON codes to human-readable names for Sentry.
     * See https://developer.android.com/reference/android/app/DownloadManager for all codes.
     */
    private fun dmErrorName(reason: Int): String = when (reason) {
        DownloadManager.ERROR_UNKNOWN                     -> "ERROR_UNKNOWN"
        DownloadManager.ERROR_FILE_ERROR                  -> "ERROR_FILE_ERROR"
        DownloadManager.ERROR_UNHANDLED_HTTP_CODE         -> "ERROR_UNHANDLED_HTTP_CODE"
        DownloadManager.ERROR_HTTP_DATA_ERROR             -> "ERROR_HTTP_DATA_ERROR"
        DownloadManager.ERROR_TOO_MANY_REDIRECTS          -> "ERROR_TOO_MANY_REDIRECTS"
        DownloadManager.ERROR_INSUFFICIENT_SPACE          -> "ERROR_INSUFFICIENT_SPACE"
        DownloadManager.ERROR_DEVICE_NOT_FOUND            -> "ERROR_DEVICE_NOT_FOUND"
        DownloadManager.ERROR_CANNOT_RESUME               -> "ERROR_CANNOT_RESUME"
        DownloadManager.ERROR_FILE_ALREADY_EXISTS         -> "ERROR_FILE_ALREADY_EXISTS"
        DownloadManager.PAUSED_WAITING_TO_RETRY           -> "PAUSED_WAITING_TO_RETRY"
        DownloadManager.PAUSED_WAITING_FOR_NETWORK        -> "PAUSED_WAITING_FOR_NETWORK"
        DownloadManager.PAUSED_QUEUED_FOR_WIFI            -> "PAUSED_QUEUED_FOR_WIFI"
        DownloadManager.PAUSED_UNKNOWN                    -> "PAUSED_UNKNOWN"
        else -> "HTTP_$reason"  // HTTP error codes like 403, 404, 500 come through here
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

    fun openFile(context: Context, offlineAttachment: OfflineAttachment) {
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
