package `in`.testpress.course.repository

import `in`.testpress.database.dao.OfflineAttachmentsDao
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.flow.Flow

class OfflineAttachmentsRepository(private val dao: OfflineAttachmentsDao) {
    fun getAllFiles(): Flow<List<OfflineAttachment>> = dao.getAllFiles()

    suspend fun insert(file: OfflineAttachment) = dao.insert(file)
    suspend fun updateStatus(id: Long, status: OfflineAttachmentDownloadStatus) = dao.updateStatus(id, status)
    suspend fun updateProgress(id: Long, progress: Int) = dao.updateProgress(id, progress)
    suspend fun getAttachmentById(id: Long) = dao.getAttachmentById(id)
}