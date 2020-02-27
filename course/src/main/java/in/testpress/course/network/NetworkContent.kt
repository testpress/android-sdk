package `in`.testpress.course.network

import `in`.testpress.database.ContentEntity
import `in`.testpress.models.greendao.Content


data class NetworkContent(
    val id: Long,
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val order: Int,
    val url: String = "",
    val chapterId: Long? = null,
    val chapterSlug: String = "",
    val chapterUrl: String = "",
    val courseId: Long? = null,
    val freePreview: Boolean = false,
    val modified: String? = null,
    val contentType: String,
    val examUrl: String = "",
    val videoUrl: String = "",
    val attachmentUrl: String = "",
    val htmlUrl: String = "",
    val isLocked: Boolean,
    val isScheduled: Boolean,
    val attemptsCount: Int = 0,
    val bookmarkId: Long? = null,
    val videoWatchedPercentage: Int = 0,
    val active: Boolean,
    val examId: Long? = null,
    val attachmentId: Long? = null,
    val videoId: Long? = null,
    val htmlId: Long? = null,
    val start: String? = null,
    val end: String? = null,
    val htmlContentTitle: String? = null,
    val htmlContentUrl: String? = null,
    val attemptsUrl: String? = null,
    val hasStarted: Boolean,
    val coverImage: String? = null
)


fun NetworkContent.asDatabaseModel(): ContentEntity {
    return ContentEntity(
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

fun NetworkContent.asGreenDaoModel(): Content {
    return Content(
        this.order,
        this.htmlContentTitle,
        this.htmlContentUrl,
        this.url,
        this.attemptsUrl,
        this.chapterSlug,
        this.chapterUrl,
        this.id,
        this.title,
        this.contentType,
        this.image,
        this.description,
        this.isLocked,
        this.attemptsCount,
        this.start,
        this.end,
        this.hasStarted,
        this.active,
        this.bookmarkId,
        this.videoWatchedPercentage,
        this.modified,
        null,
        this.freePreview,
        this.isScheduled,
        this.coverImage,
        this.courseId,
        this.chapterId,
        this.htmlId,
        this.videoId,
        this.attachmentId,
        this.examId
    )
}