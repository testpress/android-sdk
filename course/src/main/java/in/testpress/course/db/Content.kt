package `in`.testpress.course.db

import `in`.testpress.course.domain_models.DomainContent
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

fun Content.asDomainModel(): DomainContent {
    return DomainContent(
            id=this.id,
            title=this.title,
            description=this.description,
            image=this.image,
            order=this.order,
            url=this.url,
            chapterId=this.chapterId,
            chapterSlug=this.chapterSlug,
            chapterUrl=this.chapterUrl,
            courseId=this.courseId,
            freePreview=this.freePreview,
            modified=this.modified,
            contentType=this.contentType,
            examUrl=this.examUrl,
            videoUrl=this.videoUrl,
            attachmentUrl=this.attachmentUrl,
            htmlUrl=this.htmlUrl,
            isLocked=this.isLocked,
            isScheduled=this.isScheduled,
            attemptsCount=this.attemptsCount,
            bookmarkId=this.bookmarkId,
            videoWatchedPercentage=this.videoWatchedPercentage,
            active=this.active,
            examId=this.examId,
            attachmentId=this.attachmentId,
            videoId=this.videoId,
            htmlId=this.htmlId,
            start=this.start
    )
}