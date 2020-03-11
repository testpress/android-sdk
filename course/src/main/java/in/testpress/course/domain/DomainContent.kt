package `in`.testpress.course.domain

import `in`.testpress.database.ContentEntity
import `in`.testpress.database.ContentWithRelations
import `in`.testpress.models.greendao.Attachment
import `in`.testpress.models.greendao.Content

data class DomainContent(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val order: Int? = null,
    val url: String? = "",
    val chapterId: Long? = null,
    val chapterSlug: String? = null,
    val chapterUrl: String? = null,
    val courseId: Long? = null,
    val freePreview: Boolean? = null,
    val modified: String? = null,
    val contentType: String? = null,
    val examUrl: String? = null,
    val videoUrl: String? = null,
    val attachmentUrl: String? = null,
    val htmlUrl: String? = null,
    val isLocked: Boolean?,
    val isScheduled: Boolean?,
    val attemptsCount: Int? = 0,
    var bookmarkId: Long? = null,
    val videoWatchedPercentage: Int? = null,
    val active: Boolean?,
    val examId: Long? = null,
    val attachmentId: Long? = null,
    val videoId: Long? = null,
    val htmlId: Long? = null,
    val start: String? = null,
    val end: String? = null,
    val htmlContentTitle: String? = null,
    val htmlContentUrl: String? = null,
    val attemptsUrl: String? = null,
    val hasStarted: Boolean?,
    val coverImage: String? = null,
    val attachment: DomainAttachmentContent? = null,
    val htmlContent: DomainHtmlContent? = null,
    var exam: DomainExamContent? = null,
    val video: DomainVideoContent? = null
)

fun createDomainContent(contentEntity: ContentWithRelations): DomainContent {
    return DomainContent(
        id = contentEntity.content.id,
        title = contentEntity.content.title,
        description = contentEntity.content.description,
        image = contentEntity.content.image,
        order = contentEntity.content.order,
        url = contentEntity.content.url,
        chapterId = contentEntity.content.chapterId,
        chapterSlug = contentEntity.content.chapterSlug,
        chapterUrl = contentEntity.content.chapterUrl,
        courseId = contentEntity.content.courseId,
        freePreview = contentEntity.content.freePreview,
        modified = contentEntity.content.modified,
        contentType = contentEntity.content.contentType,
        examUrl = contentEntity.content.examUrl,
        videoUrl = contentEntity.content.videoUrl,
        attachmentUrl = contentEntity.content.attachmentUrl,
        htmlUrl = contentEntity.content.htmlUrl,
        attemptsUrl = contentEntity.content.attemptsUrl,
        isLocked = contentEntity.content.isLocked,
        isScheduled = contentEntity.content.isScheduled,
        attemptsCount = contentEntity.content.attemptsCount,
        bookmarkId = contentEntity.content.bookmarkId,
        videoWatchedPercentage = contentEntity.content.videoWatchedPercentage,
        active = contentEntity.content.active,
        examId = contentEntity.content.examId,
        attachmentId = contentEntity.content.attachmentId,
        videoId = contentEntity.content.videoId,
        htmlId = contentEntity.content.htmlId,
        start = contentEntity.content.start,
        hasStarted = contentEntity.content.hasStarted,
        attachment = contentEntity.attachment?.asDomainAttachment(),
        htmlContent = contentEntity.htmlContent?.asDomainHtmlContent(),
        video = contentEntity.video?.asDomainVideo(),
        exam = contentEntity.exam?.asDomainExamContent()
    )
}

fun createDomainContent(content: Content): DomainContent {
    val attachment: Attachment? = content.rawAttachment
    return DomainContent(
        id = content.id,
        title = content.title,
        description = content.description,
        image = content.image,
        order = content.order,
        url = content.url,
        chapterId = content.chapterId,
        chapterSlug = content.chapterSlug,
        chapterUrl = content.chapterUrl,
        courseId = content.courseId,
        freePreview = content.freePreview,
        modified = content.modified,
        contentType = content.contentType,
        examUrl = null,
        videoUrl = null,
        attachmentUrl = null,
        htmlUrl = null,
        isLocked = content.isLocked,
        isScheduled = content.isScheduled,
        attemptsCount = content.attemptsCount,
        bookmarkId = content.bookmarkId,
        videoWatchedPercentage = content.videoWatchedPercentage,
        active = content.active,
        examId = content.examId,
        attachmentId = content.attachmentId,
        videoId = content.videoId,
        htmlId = content.htmlId,
        start = content.start,
        hasStarted = content.hasStarted,
        attachment = attachment?.asDomainAttachment(),
        htmlContent = content.rawHtmlContent?.asDomainAttachment(),
        exam = content.rawExam?.asDomainAttachment(),
        video = content.rawVideo?.asDomainVideo(),
        attemptsUrl = content.attemptsUrl
    )
}

fun ContentWithRelations.asDomainContent(): DomainContent {
    return createDomainContent(this)
}

fun List<ContentWithRelations>.asDomainContent(): List<DomainContent> {
    return this.map {
        createDomainContent(it)
    }
}

fun Content.asDomainContent(): DomainContent {
    return createDomainContent(this)
}

fun List<Content>.asDomainContents(): List<DomainContent> {
    return this.map {
        createDomainContent(it)
    }
}