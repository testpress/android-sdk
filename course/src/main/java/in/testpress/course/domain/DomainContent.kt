package `in`.testpress.course.domain

import `in`.testpress.database.ContentEntity

data class DomainContent(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val order: Int? = null,
    val url: String = "",
    val chapterId: Long? = null,
    val chapterSlug: String = "",
    val chapterUrl: String? = null,
    val courseId: Long? = null,
    val freePreview: Boolean? = null,
    val modified: String? = null,
    val contentType: String,
    val examUrl: String? = null,
    val videoUrl: String? = null,
    val attachmentUrl: String? = null,
    val htmlUrl: String? = null,
    val isLocked: Boolean,
    val isScheduled: Boolean,
    val attemptsCount: Int = 0,
    val bookmarkId: Long? = null,
    val videoWatchedPercentage: Int? = null,
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

fun createDomainContent(contentEntity: ContentEntity): DomainContent {
    return DomainContent(
        id = contentEntity.id,
        title = contentEntity.title,
        description = contentEntity.description,
        image = contentEntity.image,
        order = contentEntity.order,
        url = contentEntity.url,
        chapterId = contentEntity.chapterId,
        chapterSlug = contentEntity.chapterSlug,
        chapterUrl = contentEntity.chapterUrl,
        courseId = contentEntity.courseId,
        freePreview = contentEntity.freePreview,
        modified = contentEntity.modified,
        contentType = contentEntity.contentType,
        examUrl = contentEntity.examUrl,
        videoUrl = contentEntity.videoUrl,
        attachmentUrl = contentEntity.attachmentUrl,
        htmlUrl = contentEntity.htmlUrl,
        isLocked = contentEntity.isLocked,
        isScheduled = contentEntity.isScheduled,
        attemptsCount = contentEntity.attemptsCount,
        bookmarkId = contentEntity.bookmarkId,
        videoWatchedPercentage = contentEntity.videoWatchedPercentage,
        active = contentEntity.active,
        examId = contentEntity.examId,
        attachmentId = contentEntity.attachmentId,
        videoId = contentEntity.videoId,
        htmlId = contentEntity.htmlId,
        start = contentEntity.start,
        hasStarted = contentEntity.hasStarted
    )
}

fun ContentEntity.asDomainContent(): DomainContent {
    return createDomainContent(this)
}

fun List<ContentEntity>.asDomainContent(): List<DomainContent> {
    return this.map {
        createDomainContent(it)
    }
}