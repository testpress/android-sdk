package `in`.testpress.database

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VideoSyncStatus {
    NOT_SYNCED, SYNCING, SUCCESS, FAILURE
}

@Entity
data class OfflineVideo(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val remoteThumbnail: String? = null,
    val localThumbnail: String? = null,
    val duration: String = "00:00:00",
    val url: String? = null,
    val contentId: Long? = null,
    var percentageDownloaded: Int = 0,
    var bytesDownloaded: Long = 0,
    var totalSize: Long = 0,
    val courseId: Long? = null,
    var lastWatchPosition: String? = null,
    var watchedTimeRanges: List<Array<String>> = arrayListOf(),
    var syncState: VideoSyncStatus = VideoSyncStatus.NOT_SYNCED
)