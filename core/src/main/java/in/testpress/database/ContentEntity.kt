package `in`.testpress.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = AttachmentEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("attachmentId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ExamContentEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("examId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = HtmlContentEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("htmlId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = VideoContentEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("videoId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ContentEntity(
    @PrimaryKey val id: Long,
    var title: String? = null,
    var description: String? = null,
    var image: String? = null,
    var order: Int? = null,
    var url: String? = null,
    var chapterId: Long? = null,
    var chapterSlug: String? = null,
    var chapterUrl: String? = null,
    var courseId: Long? = null,
    var freePreview: Boolean? = null,
    var modified: String? = null,
    var contentType: String? = null,
    var examUrl: String? = null,
    var videoUrl: String? = null,
    var attachmentUrl: String? = null,
    var htmlUrl: String? = null,
    var isLocked: Boolean? = null,
    var isScheduled: Boolean? = null,
    var attemptsCount: Int? = 0,
    var bookmarkId: Long? = null,
    var videoWatchedPercentage: Int? = null,
    var active: Boolean? = null,
    var examId: Long? = null,
    var attachmentId: Long? = null,
    var videoId: Long? = null,
    var htmlId: Long? = null,
    var start: String? = null,
    var hasStarted: Boolean? = null,
    var attachment: AttachmentEntity? = null,
    var exam: ExamContentEntity? = null,
    var htmlContent: HtmlContentEntity? = null,
    var video: VideoContentEntity? = null
)
