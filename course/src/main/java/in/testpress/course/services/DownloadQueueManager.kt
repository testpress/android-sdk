package `in`.testpress.course.services

import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.database.entities.OfflineAttachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.*

class DownloadQueueManager(
    private val scope: CoroutineScope,
    private val repo: OfflineAttachmentsRepository
) {
    private val queue = LinkedList<OfflineAttachment>()
    private var isDownloading = false

    fun enqueue(file: OfflineAttachment) {
        queue.add(file)
        scope.launch { repo.updateStatus(file.id, OfflineAttachmentDownloadStatus.QUEUED) }
        tryStartNext()
    }

    private fun tryStartNext() {
        if (isDownloading || queue.isEmpty()) return

        val next = queue.poll()
        isDownloading = true
        scope.launch {
            repo.updateStatus(next.id, OfflineAttachmentDownloadStatus.DOWNLOADING)
            try {
                download(next)
                repo.updateStatus(next.id, OfflineAttachmentDownloadStatus.COMPLETED)
            } catch (e: Exception) {
                repo.updateStatus(next.id, OfflineAttachmentDownloadStatus.FAILED)
            }
            isDownloading = false
            tryStartNext()
        }
    }

    private suspend fun download(file: OfflineAttachment) {
        // Simplified download logic using OkHttp
        val request = Request.Builder().url(file.url).build()
        val response = OkHttpClient().newCall(request).execute()
        val input = response.body?.byteStream() ?: return

        val outputFile = File(file.path)
        outputFile.outputStream().use { output ->
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            var totalBytes = 0L

            while (input.read(buffer).also { bytesRead = it } >= 0) {
                output.write(buffer, 0, bytesRead)
                totalBytes += bytesRead
                val progress = (totalBytes * 100 / (response.body?.contentLength() ?: 1)).toInt()
                repo.updateProgress(file.id, progress)
            }
        }
    }
}
