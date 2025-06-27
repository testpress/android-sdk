package `in`.testpress.course.repository

import `in`.testpress.course.services.DownloadItem
import `in`.testpress.course.services.DownloadQueueManager
import `in`.testpress.database.dao.OfflineAttachmentsDao
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
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

    suspend fun getAll(): List<OfflineAttachment> = dao.getAll()

    fun getAllFiles(): Flow<List<OfflineAttachment>> = dao.getAllFiles()

    suspend fun insert(file: OfflineAttachment) = dao.insert(file)

    suspend fun update(file: OfflineAttachment) = dao.update(file)

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun updateStatus(id: Long, status: OfflineAttachmentDownloadStatus) =
        dao.updateStatus(id, status)

    suspend fun updateProgressWithDownloadId(downloadId: Long, progress: Int) = dao.updateProgressWithDownloadId(downloadId, progress)

    suspend fun updateStatusWithDownloadId(downloadId: Long, status: OfflineAttachmentDownloadStatus) = dao.updateProgressWithDownloadId(downloadId, status)

    suspend fun getByDownloadId(downloadId: Long) = dao.getByDownloadId(downloadId)

    suspend fun updateContentUri(id: Long, contentUri: String) = dao.updateContentUri(id, contentUri)

    suspend fun updateFilePath(id: Long, path: String) = dao.updateFilePath(id, path)

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
            //updateProgress(item.id, progress)
        }
    }

    override fun onDownloadCompleted(item: DownloadItem) {
        scope.launch {
            updateStatus(item.id, OfflineAttachmentDownloadStatus.COMPLETED)
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
            }
        }
    }

    override fun onDownloadFileInfoUpdated(
        item: DownloadItem,
        localPath: String,
        displayName: String,
        contentUri: String
    ) {
        scope.launch {
            updateContentUri(item.id, contentUri)
            updateFilePath(item.id, localPath)
        }
    }
}