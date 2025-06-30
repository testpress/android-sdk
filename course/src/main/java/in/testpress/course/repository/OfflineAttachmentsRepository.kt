package `in`.testpress.course.repository

import `in`.testpress.database.dao.OfflineAttachmentsDao
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.flow.Flow

class OfflineAttachmentsRepository(private val dao: OfflineAttachmentsDao) {

    suspend fun getAll(): List<OfflineAttachment> = dao.getAll()

    fun getAllFiles(): Flow<List<OfflineAttachment>> = dao.getAllFiles()

    suspend fun insert(file: OfflineAttachment) = dao.insert(file)

    suspend fun update(file: OfflineAttachment) = dao.update(file)

    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun updateProgressWithDownloadId(downloadId: Long, progress: Int) = dao.updateProgressWithDownloadId(downloadId, progress)

    suspend fun updateStatusWithDownloadId(downloadId: Long, status: OfflineAttachmentDownloadStatus) = dao.updateProgressWithDownloadId(downloadId, status)

    suspend fun updateFilePathWithDownloadId(downloadId: Long, path: String) = dao.updateFilePathWithDownloadId(downloadId, path)

    suspend fun getByDownloadId(downloadId: Long) = dao.getByDownloadId(downloadId)
    fun getAttachment(id: Long) = dao.getAttachment(id)

    suspend fun getAllWithStatus(status: OfflineAttachmentDownloadStatus) =
        dao.getAllWithStatus(status)
}