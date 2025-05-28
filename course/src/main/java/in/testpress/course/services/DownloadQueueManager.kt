package `in`.testpress.course.services

import android.content.Context
import android.util.Log
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.ensureActive
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CopyOnWriteArrayList

data class DownloadItem(val id: Long, val url: String, val file: String)

object DownloadQueueManager {

    interface Callback {
        fun onDownloadStarted(item: DownloadItem)
        fun onProgress(item: DownloadItem, progress: Int)
        fun onDownloadCompleted(item: DownloadItem)
        fun onDownloadFailed(item: DownloadItem, error: Throwable)
        fun onDownloadCancelled(item: DownloadItem)
    }

    private var callback: Callback? = null
    private val downloadQueue = CopyOnWriteArrayList<DownloadItem>()
    private val downloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val okHttpClient = OkHttpClient()

    private var isDownloading = false
    private var currentDownloadJob: Job? = null
    private var currentItem: DownloadItem? = null

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun enqueue(item: DownloadItem) {
        downloadQueue.add(item)
        processQueue()
    }

    fun clearQueue() {
        downloadQueue.clear()
    }

    private fun processQueue() {
        if (isDownloading || downloadQueue.isEmpty()) return

        isDownloading = true
        val item = downloadQueue.removeAt(0)
        currentItem = item

        currentDownloadJob = downloadScope.launch {
            try {
                callback?.onDownloadStarted(item)
                downloadFile(item)
                callback?.onDownloadCompleted(item)
            } catch (e: CancellationException) {
                Log.w("DownloadQueueManager", "Download cancelled: ${item.url}")
                callback?.onDownloadCancelled(item)
            } catch (e: Exception) {
                Log.e("DownloadQueueManager", "Failed to download: ${item.url}", e)
                callback?.onDownloadFailed(item, e)
            } finally {
                isDownloading = false
                currentItem = null
                currentDownloadJob = null
                processQueue()
            }
        }
    }

    private suspend fun downloadFile(item: DownloadItem) =
        withContext(downloadScope.coroutineContext) {
            val request = Request.Builder()
                .url(item.url)
                .build()

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("Failed to download file: ${response.code}")
            }

            val body = response.body ?: throw IOException("Empty response body")
            val contentLength = body.contentLength()
            if (contentLength <= 0) {
                Log.w("DownloadQueueManager", "Invalid contentLength: $contentLength")
            }

            body.byteStream().use { input ->

                val target = File(item.file)
                target.parentFile?.mkdirs()

                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastProgress = -1

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        currentDownloadJob?.ensureActive()
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        if (contentLength > 0) {
                            val progress = (totalRead * 100 / contentLength).toInt()
                            if (progress != lastProgress) {
                                lastProgress = progress
                                callback?.onProgress(item, progress)
                            }
                        }
                    }
                }
            }
        }

    private fun cancelCurrent() {
        currentDownloadJob?.cancel()
    }

    fun cancelDownloadById(id: Long) {
        val current = currentItem
        if (current?.id == id) {
            cancelCurrent()
            return
        }

        val removed = downloadQueue.find { it.id == id }
        if (removed != null) {
            downloadQueue.remove(removed)
            callback?.onDownloadCancelled(removed)
        }
    }

    fun cancelAll() {
        currentDownloadJob?.cancel()
        downloadScope.coroutineContext.cancelChildren()
        clearQueue()
        isDownloading = false
    }

    fun restartPendingDownloads(context: Context) {
        downloadScope.launch {
            val dao = TestpressDatabase.invoke(context.applicationContext).offlineAttachmentDao()
            val repo = OfflineAttachmentsRepository(dao, downloadScope)
            val downloadingAttachments =
                repo.getAllWithStatus(OfflineAttachmentDownloadStatus.DOWNLOADING)
            val queuedAttachments = repo.getAllWithStatus(OfflineAttachmentDownloadStatus.QUEUED)
            downloadingAttachments.forEach { attachment ->
                enqueue(DownloadItem(attachment.id, attachment.url, attachment.path))
            }
            queuedAttachments.forEach { attachment ->
                enqueue(DownloadItem(attachment.id, attachment.url, attachment.path))
            }
        }
    }
}