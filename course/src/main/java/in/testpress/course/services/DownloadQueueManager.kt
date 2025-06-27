package `in`.testpress.course.services

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.util.getMimeTypeFromUrl
import kotlinx.coroutines.*
import kotlinx.coroutines.ensureActive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.CopyOnWriteArrayList

data class DownloadItem(val id: Long, val url: String, val file: String)

object DownloadQueueManager {

    interface Callback {
        fun onDownloadStarted(item: DownloadItem)
        fun onProgress(item: DownloadItem, progress: Int)
        fun onDownloadCompleted(item: DownloadItem)
        fun onDownloadFailed(item: DownloadItem, error: Throwable)
        fun onDownloadCancelled(item: DownloadItem)
        fun onDownloadFileInfoUpdated(item: DownloadItem, localPath: String, displayName: String, contentUri: String)
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

    fun enqueue(context: Context, item: DownloadItem) {
        downloadQueue.add(item)
        processQueue(context)
    }

    fun clearQueue() {
        downloadQueue.clear()
    }

    private fun processQueue(context: Context) {
        if (isDownloading || downloadQueue.isEmpty()) return

        isDownloading = true
        val item = downloadQueue.removeAt(0)
        currentItem = item

        currentDownloadJob = downloadScope.launch {
            try {
                callback?.onDownloadStarted(item)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    downloadFile(context, item)
                } else {
                    downloadFile(item)
                }
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
                processQueue(context)
            }
        }
    }

    private suspend fun downloadFile(item: DownloadItem) =
        withContext(downloadScope.coroutineContext) {
            val request = Request.Builder().url(item.url).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) throw IOException("Failed: ${response.code}")
            val body = response.body ?: throw IOException("Empty response body")

            val target = File(item.file)
            target.parentFile?.mkdirs()

            try {
                downloadAndWriteStream(item, body) {
                    FileOutputStream(target)
                }
            } catch (e: CancellationException) {
                target.delete()
                throw e
            } finally {
                response.close()
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun downloadFile(context: Context, item: DownloadItem) =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val fileName = File(item.file).name
            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, getMimeTypeFromUrl(item.url))
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val fileUri = resolver.insert(collection, contentValues)
                ?: throw IOException("Failed to create MediaStore entry")

            val request = Request.Builder().url(item.url).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) throw IOException("HTTP error: ${response.code}")
            val body = response.body ?: throw IOException("Empty response body")

            try {
                downloadAndWriteStream(item, body) {
                    resolver.openOutputStream(fileUri)
                        ?: throw IOException("Failed to open output stream")
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(fileUri, contentValues, null, null)

                resolver.query(fileUri, arrayOf(
                    MediaStore.Downloads.DISPLAY_NAME,
                    MediaStore.Downloads.DATA
                ), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME))
                        val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA))
                        callback?.onDownloadFileInfoUpdated(item, path, name, fileUri.toString())
                    }
                }
            } catch (e: CancellationException) {
                resolver.delete(fileUri, null, null)
                throw e
            } finally {
                response.close()
            }
        }

    private suspend fun downloadAndWriteStream(
        item: DownloadItem,
        responseBody: ResponseBody,
        outputStreamProvider: suspend () -> OutputStream
    ) {
        val contentLength = responseBody.contentLength()
        val input = responseBody.byteStream()

        outputStreamProvider().use { output ->
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

        input.close()
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
                enqueue(context, DownloadItem(attachment.id, attachment.url, attachment.path))
            }
            queuedAttachments.forEach { attachment ->
                enqueue(context, DownloadItem(attachment.id, attachment.url, attachment.path))
            }
        }
    }
}