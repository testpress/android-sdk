package `in`.testpress.course.repository

import `in`.testpress.course.services.DownloadItem
import `in`.testpress.course.services.DownloadQueueManager
import `in`.testpress.database.dao.OfflineAttachmentsDao
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.util.deleteFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OfflineAttachmentsRepository(
    private val dao: OfflineAttachmentsDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) :
    DownloadQueueManager.Callback {

    init {
        DownloadQueueManager.setCallback(this)
    }

    fun getAllFiles(): Flow<List<OfflineAttachment>> = dao.getAllFiles()

    suspend fun insert(file: OfflineAttachment) = dao.insert(file)

    suspend fun delete(id: Long) = dao.deleteById(id)

    private suspend fun updateStatus(id: Long, status: OfflineAttachmentDownloadStatus) =
        dao.updateStatus(id, status)

    private suspend fun updateProgress(id: Long, progress: Int) = dao.updateProgress(id, progress)

    suspend fun getAttachmentById(id: Long) = dao.getAttachmentById(id)

    fun getAttachment(id: Long) = dao.getAttachment(id)

    suspend fun getAllWithStatus(status: OfflineAttachmentDownloadStatus) =
        dao.getAllWithStatus(status)

    override fun onDownloadStarted(item: DownloadItem) {
        scope.launch {
            updateStatus(item.id, OfflineAttachmentDownloadStatus.DOWNLOADING)
        }
    }

    override fun onProgress(item: DownloadItem, progress: Int) {
        scope.launch {
            updateProgress(item.id, progress)
        }
    }

    override fun onDownloadCompleted(item: DownloadItem) {
        scope.launch {
            updateStatus(item.id, OfflineAttachmentDownloadStatus.DOWNLOADED)
        }
    }

    override fun onDownloadFailed(item: DownloadItem, error: Throwable) {
        scope.launch {
            updateStatus(item.id, OfflineAttachmentDownloadStatus.FAILED)
        }
    }

    override fun onDownloadCancelled(item: DownloadItem) {
        scope.launch {
            val attachment = getAttachmentById(item.id)
            attachment?.let {
                delete(item.id)
                deleteFile(it.path)
            }
        }
    }
}