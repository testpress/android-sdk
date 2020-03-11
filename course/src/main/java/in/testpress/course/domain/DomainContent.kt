package `in`.testpress.course.domain

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.database.ContentEntity
import `in`.testpress.models.greendao.Attachment
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import android.content.Context

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
        attachment = attachment?.asDomainContent(),
        htmlContent = content.rawHtmlContent?.asDomainContent(),
        exam = content.rawExam?.asDomainContent(),
        video = content.rawVideo?.asDomainContent(),
        attemptsUrl = content.attemptsUrl
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

fun Content.asDomainContent(): DomainContent {
    return createDomainContent(this)
}

fun List<Content>.asDomainContents(): List<DomainContent> {
    return this.map {
        createDomainContent(it)
    }
}

fun DomainContent.getGreenDaoContent(context: Context): Content? {
    val contentDao = TestpressSDKDatabase.getContentDao(context)
    val contents =  contentDao.queryBuilder().where(ContentDao.Properties.Id.eq(this.id)).list()
    if (contents.isNotEmpty()) {
        return contents[0]
    }

    return null
}