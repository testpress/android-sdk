package `in`.testpress.course.services

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
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

        fun onUpdate(itemId: Long, url:String?, fileName: String?)
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
                    downloadForScopedStorage(context, item)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun downloadForScopedStorage(context: Context, item: DownloadItem) = withContext(Dispatchers.IO) {
        val resolver = context.applicationContext.contentResolver

        val projection = arrayOf(
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.DATA,
        )

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, File(item.file).name)
            put(MediaStore.Downloads.MIME_TYPE, getMimeTypeFromUrl(item.url))
            put(MediaStore.Downloads.RELATIVE_PATH, DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val fileUri = resolver.insert(collection, contentValues)
            ?: throw IOException("Failed to create MediaStore entry")



        Log.d("TAG", "downloadForScopedStorage: $fileUri")

        val request = Request.Builder().url(item.url).build()
        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("HTTP error: ${response.code}")
        val body = response.body ?: throw IOException("Empty response body")
        val contentLength = body.contentLength()

        resolver.openOutputStream(fileUri)?.use { output ->
            val input = body.byteStream()
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
        } ?: throw IOException("Failed to open output stream")

        // Mark the file as no longer pending
        contentValues.clear()

        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(fileUri, contentValues, null, null)

        val cursor1 = resolver.query(fileUri, projection, null, null, null)
        cursor1?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Downloads.DATA)
                val displayName = it.getString(nameIndex)
                val displayData = it.getString(dataIndex)

                callback?.onUpdate(item.id, fileUri.toString() , displayName)

                Log.d("MediaStoreQuery", "File name: $displayName Data: $displayData")
            }
        }

    }


    private fun getMimeTypeFromUrl(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
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
            val downloadingAttachments = repo.getAllWithStatus(OfflineAttachmentDownloadStatus.DOWNLOADING)
            val queuedAttachments = repo.getAllWithStatus(OfflineAttachmentDownloadStatus.QUEUED)
            downloadingAttachments.forEach {
                enqueue(context, DownloadItem(it.id, it.url, File(it.path).name))
            }
            queuedAttachments.forEach {
                enqueue(context, DownloadItem(it.id, it.url, File(it.path).name))
            }
        }
    }

    fun syncDownloadedFileWithDatabase(context: Context) {
        downloadScope.launch {
            val resolver = context.contentResolver
            val dao = TestpressDatabase.invoke(context.applicationContext).offlineAttachmentDao()
            val repo = OfflineAttachmentsRepository(dao, downloadScope)
            val offlineAttachments = repo.getAll()

            offlineAttachments.forEach { offlineAttachment ->
                try {
                    val path = offlineAttachment.path
                    if (path.isBlank()) return@forEach

                    val isDeleted = if (path.startsWith("content://")) {
                        Log.d("TAG", "syncDownloadedFileWithDatabase: if")
                        // Scoped Storage: check if content URI still exists
                        val uri = Uri.parse(path)
                        val cursor = resolver.query(uri, null, null, null, null)
                        val exists = cursor?.use { it.moveToFirst() } ?: false
                        !exists
                    } else {
                        Log.d("TAG", "syncDownloadedFileWithDatabase: else")
                        // File path for Android 9 and below
                        val file = File(path)
                        !file.exists()
                    }

                    if (isDeleted) {
                        Log.d("TAG", "syncDownloadedFileWithDatabase: isDeleted")
                        repo.updateStatus(offlineAttachment.id, OfflineAttachmentDownloadStatus.DELETE)
                        Log.i("DownloadQueueManager", "Marked as DELETED: $path")
                    }
                    Log.d("TAG", "syncDownloadedFileWithDatabase: !isDeleted")
                } catch (e: Exception) {
                    Log.e("DownloadQueueManager", "Error checking file for attachment ID: ${offlineAttachment.id}", e)
                }
            }
        }
    }
}
