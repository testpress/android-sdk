package `in`.testpress.course.repository

import `in`.testpress.course.services.DownloadItem
import `in`.testpress.course.services.DownloadQueueManager
import `in`.testpress.course.util.FileUtils.deleteFile
import `in`.testpress.database.dao.OfflineAttachmentsDao
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.flow.Flow

class OfflineAttachmentsRepository(private val dao: OfflineAttachmentsDao) :
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
    suspend fun getAllWithStatus(status: OfflineAttachmentDownloadStatus) =
        dao.getAllWithStatus(status)

    override suspend fun onDownloadStarted(item: DownloadItem) {
        updateStatus(item.id, OfflineAttachmentDownloadStatus.DOWNLOADING)
    }

    override suspend fun onProgress(item: DownloadItem, progress: Int) {
        updateProgress(item.id, progress)
    }

    override suspend fun onDownloadCompleted(item: DownloadItem) {
        updateStatus(item.id, OfflineAttachmentDownloadStatus.DOWNLOADED)
    }

    override suspend fun onDownloadFailed(item: DownloadItem, error: Throwable) {
        updateStatus(item.id, OfflineAttachmentDownloadStatus.FAILED)
    }

    override suspend fun onDownloadCancelled(item: DownloadItem) {
        val attachment = getAttachmentById(item.id)
        delete(item.id)
        attachment?.let {
            deleteFile(it.path)
        }
    }
}