package `in`.testpress.database.entities

import android.content.Context
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import `in`.testpress.util.getMimeTypeFromUri
import java.io.File

@Entity
data class OfflineAttachment(
    @PrimaryKey val id: Long,
    val title: String,
    val url: String,
    val path: String,
    val contentUri: String?,
    val downloadId: Long,
    val status: OfflineAttachmentDownloadStatus,
    val progress: Int = 0
)

enum class OfflineAttachmentDownloadStatus {
    QUEUED, DOWNLOADING, FAILED, COMPLETED, DELETE
}

fun OfflineAttachment.getLocalPath(): String? {
    return Uri.parse(path).path
}

fun OfflineAttachment.getLocalFile(): File? {
    val localPath = getLocalPath() ?: return null
    return File(localPath)
}

fun OfflineAttachment.isPdf(context: Context? = null): Boolean {
    val mimeType = if (context != null) {
        getMimeTypeFromUri(context, path, contentUri)
    } else null
    return mimeType?.equals("application/pdf", ignoreCase = true) == true ||
            title.endsWith(".pdf", ignoreCase = true) ||
            path.endsWith(".pdf", ignoreCase = true)
}
