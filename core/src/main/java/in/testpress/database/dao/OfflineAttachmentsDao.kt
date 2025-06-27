package `in`.testpress.database.dao

import androidx.room.*
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineAttachmentsDao {
    @Query("SELECT * FROM OfflineAttachment ORDER BY id DESC")
    fun getAllFiles(): Flow<List<OfflineAttachment>>

    @Query("SELECT * FROM OfflineAttachment ORDER BY id DESC")
    suspend fun getAll(): List<OfflineAttachment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: OfflineAttachment)

    @Update
    suspend fun update(file: OfflineAttachment)

    @Query("DELETE FROM OfflineAttachment WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE OfflineAttachment SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: OfflineAttachmentDownloadStatus)

    @Query("UPDATE OfflineAttachment SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int)

    @Query("UPDATE OfflineAttachment SET contentUri = :contentUri WHERE id = :id")
    suspend fun updateContentUri(id: Long, contentUri: String)

    @Query("UPDATE OfflineAttachment SET path = :path WHERE id = :id")
    suspend fun updateFilePath(id: Long, path: String)

    @Query("SELECT * FROM OfflineAttachment WHERE id = :id")
    suspend fun getAttachmentById(id: Long): OfflineAttachment?

    @Query("SELECT * FROM OfflineAttachment WHERE id = :id")
    fun getAttachment(id: Long): Flow<OfflineAttachment?>

    @Query("SELECT * FROM OfflineAttachment WHERE downloadId = :downloadId")
    suspend fun getByDownloadId(downloadId: Long): OfflineAttachment?

    @Query("SELECT * FROM OfflineAttachment WHERE status =:status ORDER BY id DESC")
    suspend fun getAllWithStatus(status: OfflineAttachmentDownloadStatus): List<OfflineAttachment>

    @Query("UPDATE OfflineAttachment SET progress = :progress WHERE downloadId = :downloadId")
    suspend fun updateProgressWithDownloadId(downloadId: Long, progress: Int)

    @Query("UPDATE OfflineAttachment SET status = :status WHERE downloadId = :downloadId")
    suspend fun updateProgressWithDownloadId(downloadId: Long, status: OfflineAttachmentDownloadStatus)
}