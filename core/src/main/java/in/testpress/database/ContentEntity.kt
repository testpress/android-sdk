package `in`.testpress.database

import `in`.testpress.database.entities.BaseContentEntity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContentEntity(
    @PrimaryKey val id: Long,
    var description: String? = null,
    var image: String? = null,
    var url: String = "",
    var chapterSlug: String = "",
    var chapterUrl: String? = null,
    var modified: String? = null,
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
    var htmlId: Long? = null,
    var hasStarted: Boolean,
    var isCourseAvailable: Boolean? = null,
    var coverImageSmall: String? = null,
    var coverImageMedium: String? = null,
    var coverImage: String? = null,
    var nextContentId: Long? = null,
    var hasEnded: Boolean? = null,
    var examStartUrl: String? = null
): BaseContentEntity()
