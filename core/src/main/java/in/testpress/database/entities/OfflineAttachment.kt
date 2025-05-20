package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class OfflineAttachment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val url: String,
    val path: String,
    val status: OfflineAttachmentDownloadStatus,
    val progress: Int = 0
)

enum class OfflineAttachmentDownloadStatus {
    QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELED
}