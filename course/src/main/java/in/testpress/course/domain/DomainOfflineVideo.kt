package `in`.testpress.course.domain

import `in`.testpress.database.OfflineVideo
import `in`.testpress.database.VideoSyncStatus

data class DomainOfflineVideo(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val remoteThumbnail: String? = null,
    val localThumbnail: String? = null,
    val duration: String = "00:00:00",
    val url: String? = null,
    val contentId: Long? = null,
    val percentageDownloaded: Int = 0,
    val bytesDownloaded: Long = 0,
    val totalSize: Long = 0,
    val courseId: Long = -1,
    val syncState: VideoSyncStatus = VideoSyncStatus.NOT_SYNCED
) {
    val isDownloadCompleted = percentageDownloaded == 100
}

fun List<OfflineVideo>.asDomainModel(): List<DomainOfflineVideo> {
    return this.map {
        it.asDomainModel()
    }
}

fun OfflineVideo.asDomainModel(): DomainOfflineVideo {
    return DomainOfflineVideo(
        id = id,
        title = title,
        description = description,
        remoteThumbnail = remoteThumbnail,
        localThumbnail = localThumbnail,
        duration = duration,
        url = url,
        contentId = contentId,
        percentageDownloaded = percentageDownloaded,
        bytesDownloaded = bytesDownloaded,
        totalSize = totalSize,
        courseId = courseId!!,
        syncState = syncState
    )
}
