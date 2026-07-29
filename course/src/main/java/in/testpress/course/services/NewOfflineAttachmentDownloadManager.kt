package `in`.testpress.course.services

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.util.getFileExtensionFromUrl
import `in`.testpress.util.getMimeTypeFromUri
import `in`.testpress.util.openFile
import `in`.testpress.util.sanitizeFileName
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import `in`.testpress.course.domain.NewOfflineAttachmentDownloadFailureReason
import `in`.testpress.course.domain.NewOfflineAttachmentDownloadListener
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class HttpDownloadException(val responseCode: Int, message: String) : IOException(message)


class NewOfflineAttachmentDownloader(
    private val uri: Uri,
    private val outputFilePath: String,
    private val connectTimeout: Int = 30000,
    private val readTimeout: Int = 30000
) {
    private val isActive = AtomicBoolean(true)
    private var connection: HttpURLConnection? = null
    private var inputStream: InputStream? = null
    private var outputStream: FileOutputStream? = null

    interface ProgressListener {
        fun onProgress(contentLength: Long, bytesDownloaded: Long, percentDownloaded: Float)
    }

    fun download(progressListener: ProgressListener?) {
        try {
            val file = File(outputFilePath)
            val parent = file.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            var existingLength = 0L
            if (file.exists()) {
                existingLength = file.length()
            }

            val url = URL(uri.toString())
            connection = url.openConnection() as HttpURLConnection
            connection?.connectTimeout = connectTimeout
            connection?.readTimeout = readTimeout
            connection?.instanceFollowRedirects = true

            // Set Range header for resume if file already exists
            if (existingLength > 0) {
                connection?.setRequestProperty("Range", "bytes=$existingLength-")
            }

            connection?.connect()

            var responseCode = connection?.responseCode ?: -1

            // Handle 416 Range Not Satisfiable: stale or oversized partial file. Clear it and start from scratch.
            if (responseCode == 416) {
                connection?.disconnect()
                if (file.exists()) {
                    file.delete()
                }
                existingLength = 0L

                connection = url.openConnection() as HttpURLConnection
                connection?.connectTimeout = connectTimeout
                connection?.readTimeout = readTimeout
                connection?.instanceFollowRedirects = true
                connection?.connect()

                responseCode = connection?.responseCode ?: -1
            }

            val isResume = responseCode == HttpURLConnection.HTTP_PARTIAL

            // If range response is not 206 (Partial Content), rewrite file from scratch
            if (existingLength > 0 && !isResume) {
                existingLength = 0L
            }

            if (responseCode >= 400) {
                throw HttpDownloadException(responseCode, "Server returned HTTP response code: $responseCode")
            }

            inputStream = connection?.inputStream ?: throw IOException("Could not open input stream")
            outputStream = FileOutputStream(file, isResume)

            var bytesDownloaded = existingLength
            val responseContentLength = connection?.contentLengthLong ?: -1L
            val totalContentLength = if (responseContentLength > 0) {
                if (isResume) responseContentLength + existingLength else responseContentLength
            } else {
                -1L
            }

            val buffer = ByteArray(8192)
            var bytesReadThisChunk: Int

            while (isActive.get()) {
                bytesReadThisChunk = inputStream?.read(buffer) ?: -1
                if (bytesReadThisChunk == -1) break

                outputStream?.write(buffer, 0, bytesReadThisChunk)
                bytesDownloaded += bytesReadThisChunk

                val percent = if (totalContentLength > 0) {
                    (bytesDownloaded * 100f) / totalContentLength
                } else {
                    -1f
                }
                progressListener?.onProgress(totalContentLength, bytesDownloaded, percent)
            }

            outputStream?.flush()

            if (!isActive.get()) {
                throw IOException("Download cancelled by user")
            }

            // Verify content length matches bytes downloaded
            if (totalContentLength > 0 && bytesDownloaded != totalContentLength) {
                throw IOException("Incomplete download: read $bytesDownloaded of $totalContentLength bytes")
            }

        } finally {
            cleanup()
        }
    }

    fun cancel() {
        isActive.set(false)
        cleanup()
    }

    private fun cleanup() {
        try {
            inputStream?.close()
        } catch (ignored: Exception) {}
        try {
            outputStream?.close()
        } catch (ignored: Exception) {}
        try {
            connection?.disconnect()
        } catch (ignored: Exception) {}
        inputStream = null
        outputStream = null
        connection = null
    }
}

class NewOfflineAttachmentDownloadManager private constructor(private val repository: OfflineAttachmentsRepository) {

    private val progressScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dbMutex = Mutex()

    @Volatile
    private var maxParallelDownloads = 3
    private val pendingQueue = LinkedBlockingQueue<DomainAttachmentContent>()
    private val activeJobs = ConcurrentHashMap<Long, Job>()
    private val activeDownloaders = ConcurrentHashMap<Long, NewOfflineAttachmentDownloader>()
    private val listeners: MutableSet<NewOfflineAttachmentDownloadListener> = Collections.synchronizedSet(
        Collections.newSetFromMap(WeakHashMap<NewOfflineAttachmentDownloadListener, Boolean>())
    )

    fun addListener(listener: NewOfflineAttachmentDownloadListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: NewOfflineAttachmentDownloadListener) {
        listeners.remove(listener)
    }

    fun setMaxParallelDownloads(max: Int) {
        maxParallelDownloads = max
        processQueue()
    }

    fun enqueueDownload(context: Context, domainAttachmentContent: DomainAttachmentContent) {
        if (domainAttachmentContent.attachmentUrl == null) {
            Toast.makeText(context, "Attachment URL cannot be null", Toast.LENGTH_SHORT).show()
            return
        }

        // De-duplication check: if task is already running or queued, ignore the duplicate request
        if (activeJobs.containsKey(domainAttachmentContent.id) ||
            pendingQueue.any { it.id == domainAttachmentContent.id }) {
            return
        }

        val fileName = "${domainAttachmentContent.title}${getFileExtensionFromUrl(domainAttachmentContent.attachmentUrl)}".sanitizeFileName()
        val directory = File(context.filesDir, "offline_attachments")
        val destFile = File(directory, fileName)

        val offlineAttachment = OfflineAttachment(
            id = domainAttachmentContent.id,
            title = domainAttachmentContent.title ?: "Attachment ${domainAttachmentContent.id}",
            url = domainAttachmentContent.attachmentUrl,
            path = Uri.fromFile(destFile).toString(),
            contentUri = null,
            downloadId = 0L, // placeholder
            status = OfflineAttachmentDownloadStatus.QUEUED,
            progress = 0
        )

        progressScope.launch {
            dbMutex.withLock {
                repository.insert(offlineAttachment)
            }
            pendingQueue.add(domainAttachmentContent)
            processQueue()
        }
    }

    private fun processQueue() {
        progressScope.launch {
            dbMutex.withLock {
                while (activeJobs.size < maxParallelDownloads && pendingQueue.isNotEmpty()) {
                    val content = pendingQueue.poll() ?: break
                    val job = launch {
                        runDownloadTask(content)
                    }
                    activeJobs[content.id] = job
                }
            }
        }
    }

    private suspend fun runDownloadTask(content: DomainAttachmentContent) {
        try {
            val attachment = dbMutex.withLock { repository.getById(content.id) } ?: return
            val url = content.attachmentUrl ?: return
            val filePath = Uri.parse(attachment.path).path ?: return

            var attempt = 1
            val maxRetries = 5
            var succeeded = false
            var lastException: Exception? = null
            var failureReason = NewOfflineAttachmentDownloadFailureReason.FAILURE_REASON_NONE

            val downloader = NewOfflineAttachmentDownloader(Uri.parse(url), filePath)
            activeDownloaders[content.id] = downloader

            // Update DB status to DOWNLOADING before start
            dbMutex.withLock {
                val record = repository.getById(content.id)
                record?.let {
                    repository.update(it.copy(status = OfflineAttachmentDownloadStatus.DOWNLOADING))
                }
            }

            var lastUpdatedPercent = -1

            while (attempt <= maxRetries && !succeeded) {
                try {
                    downloader.download(object : NewOfflineAttachmentDownloader.ProgressListener {
                        override fun onProgress(contentLength: Long, bytesDownloaded: Long, percentDownloaded: Float) {
                            val progressPercent = if (percentDownloaded >= 0) percentDownloaded.toInt() else 0
                            
                            // Only update database when the rounded percentage changes to throttle I/O roundtrips
                            if (progressPercent != lastUpdatedPercent) {
                                lastUpdatedPercent = progressPercent
                                progressScope.launch {
                                    dbMutex.withLock {
                                        val record = repository.getById(content.id)
                                        record?.let {
                                            if (it.status != OfflineAttachmentDownloadStatus.COMPLETED &&
                                                it.status != OfflineAttachmentDownloadStatus.FAILED) {
                                                repository.update(it.copy(progress = progressPercent))
                                            }
                                        }
                                    }
                                }
                            }

                            // Dispatch progress to UI listeners immediately for smooth updates
                            synchronized(listeners) {
                                listeners.forEach {
                                    it.onDownloadProgress(content.id, bytesDownloaded, contentLength, percentDownloaded)
                                }
                            }
                        }
                    })
                    succeeded = true
                } catch (e: Exception) {
                    lastException = e
                    failureReason = when (e) {
                        is SocketTimeoutException -> NewOfflineAttachmentDownloadFailureReason.FAILURE_REASON_NETWORK
                        is HttpDownloadException -> NewOfflineAttachmentDownloadFailureReason.FAILURE_REASON_HTTP
                        is IOException -> NewOfflineAttachmentDownloadFailureReason.FAILURE_REASON_IO
                        else -> NewOfflineAttachmentDownloadFailureReason.FAILURE_REASON_UNKNOWN
                    }

                    Log.w("NewOfflineAttachmentDownloadManager", "Download ID=${content.id} failed on attempt $attempt: ${e.message}")

                    if (attempt < maxRetries) {
                        val backoffMs = Math.min(1000L * attempt, 30000L)
                        delay(backoffMs)
                        attempt++
                    } else {
                        break
                    }
                }
            }

            progressScope.launch {
                dbMutex.withLock {
                    val record = repository.getById(content.id)
                    record?.let {
                        if (succeeded) {
                            repository.update(it.copy(
                                status = OfflineAttachmentDownloadStatus.COMPLETED,
                                progress = 100
                            ))
                            synchronized(listeners) {
                                listeners.forEach { l -> l.onDownloadCompleted(content.id, filePath) }
                            }
                        } else {
                            repository.update(it.copy(
                                status = OfflineAttachmentDownloadStatus.FAILED,
                                progress = 0
                            ))
                            synchronized(listeners) {
                                listeners.forEach { l -> l.onDownloadFailed(content.id, failureReason, lastException) }
                            }
                        }
                    }
                }
                processQueue()
            }
        } finally {
            activeDownloaders.remove(content.id)
            dbMutex.withLock {
                activeJobs.remove(content.id)
            }
            processQueue()
        }
    }

    fun cancelDownload(context: Context, offlineAttachment: OfflineAttachment) {
        val downloader = activeDownloaders[offlineAttachment.id]
        downloader?.cancel()

        val job = activeJobs[offlineAttachment.id]
        job?.cancel()

        progressScope.launch {
            dbMutex.withLock {
                repository.delete(offlineAttachment.id)
            }
            val file = Uri.parse(offlineAttachment.path).path?.let { File(it) }
            if (file != null && file.exists()) {
                file.delete()
            }
            processQueue()
        }
    }

    fun deleteDownload(context: Context, offlineAttachment: OfflineAttachment) {
        cancelDownload(context, offlineAttachment)
    }

    fun openFile(context: Context, offlineAttachment: OfflineAttachment) {
        val filePathUri = offlineAttachment.path
        val contentUri = offlineAttachment.contentUri
        val mimeType = getMimeTypeFromUri(context, filePathUri, contentUri)
        openFile(context, filePathUri, contentUri, mimeType)
    }

    fun restartDownloadProgressTracking(context: Context) {
        progressScope.launch {
            val downloadingAttachments = repository.getAllWithStatus(OfflineAttachmentDownloadStatus.DOWNLOADING)
            val queuedAttachments = repository.getAllWithStatus(OfflineAttachmentDownloadStatus.QUEUED)

            val attachmentsToTrack = downloadingAttachments + queuedAttachments
            attachmentsToTrack.forEach { attachment ->
                val domainAttachmentContent = DomainAttachmentContent(
                    id = attachment.id,
                    title = attachment.title,
                    attachmentUrl = attachment.url,
                    allowDownload = true
                )
                enqueueDownload(context, domainAttachmentContent)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: NewOfflineAttachmentDownloadManager? = null

        fun init(repository: OfflineAttachmentsRepository) {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = NewOfflineAttachmentDownloadManager(repository)
                }
            }
        }

        fun getInstance(context: Context): NewOfflineAttachmentDownloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NewOfflineAttachmentDownloadManager(
                    OfflineAttachmentsRepository(
                        TestpressDatabase.invoke(context.applicationContext).offlineAttachmentDao()
                    )
                ).also { INSTANCE = it }
            }
        }

        fun getInstance(): NewOfflineAttachmentDownloadManager {
            return INSTANCE ?: throw IllegalStateException(
                "NewOfflineAttachmentDownloadManager is not initialized. Call init() in your Application class or use getInstance(context)."
            )
        }
    }
}
