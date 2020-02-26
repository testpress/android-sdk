package `in`.testpress.course.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity
data class Content (
        @PrimaryKey val id: Long,
        var title: String = "",
        var description: String = "",
        var image: String = "",
        var order: Int,
        var url: String = "",
        var chapterId: Long? = null,
        var chapterSlug: String = "",
        var chapterUrl: String = "",
        var courseId: Long? = null,
        var freePreview: Boolean = false,
        var modified: String? = null,
        var contentType: String,
        var examUrl: String = "",
        var videoUrl: String = "",
        var attachmentUrl: String = "",
        var htmlUrl: String = "",
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
        var start: String? = null
)