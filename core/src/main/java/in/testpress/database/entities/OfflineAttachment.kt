package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineAttachment(
    @PrimaryKey val id: Long,
    val title: String,
    val url: String,
    val path: String,
    val fileName: String,
    val contentUri: String?,
    val status: OfflineAttachmentDownloadStatus,
    val progress: Int = 0
)

enum class OfflineAttachmentDownloadStatus {
    QUEUED, DOWNLOADING, FAILED, COMPLETED, DELETE
}