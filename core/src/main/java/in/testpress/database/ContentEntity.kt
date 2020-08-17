package `in`.testpress.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContentEntity(
    @PrimaryKey val id: Long,
    var title: String? = null,
    var description: String? = null,
    var image: String? = null,
    var order: Int? = null,
    var url: String = "",
    var chapterId: Long? = null,
    var chapterSlug: String = "",
    var chapterUrl: String? = null,
    var courseId: Long? = null,
    var freePreview: Boolean? = null,
    var modified: String? = null,
    var contentType: String,
    var examUrl: String? = null,
    var videoUrl: String? = null,
    var attachmentUrl: String? = null,
    var htmlUrl: String? = null,
    var isLocked: Boolean,
    var isScheduled: Boolean,
    var attemptsCount: Int = 0,
    var bookmarkId: Long? = null,
    var videoWatchedPercentage: Int? = null,
    var active: Boolean,
    var examId: Long? = null,
    var attachmentId: Long? = null,
    var videoId: Long? = null,
    var htmlId: Long? = null,
    var start: String? = null,
    var hasStarted: Boolean,
    var isCourseAvailable: Boolean? = null
)
