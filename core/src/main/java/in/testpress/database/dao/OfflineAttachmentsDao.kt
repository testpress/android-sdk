package `in`.testpress.database.dao

import androidx.room.*
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineAttachmentsDao {
    @Query("SELECT * FROM OfflineAttachment ORDER BY rowid DESC")
    fun getAllFiles(): Flow<List<OfflineAttachment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: OfflineAttachment)

    @Update
    suspend fun update(file: OfflineAttachment)

    @Query("UPDATE OfflineAttachment SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: OfflineAttachmentDownloadStatus)

    @Query("UPDATE OfflineAttachment SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int)
}