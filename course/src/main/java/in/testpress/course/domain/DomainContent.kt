package `in`.testpress.course.domain

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.util.DateUtils.getFormattedDateStringOrNull
import `in`.testpress.database.ContentEntity
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.models.greendao.Attachment
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.CourseAttemptDao
import `in`.testpress.util.FormatDate
import android.content.Context
import android.text.format.DateUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.util.Util

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
    val videoConferenceID: Long? = null,
    val htmlId: Long? = null,
    val start: String? = null,
    val end: String? = null,
    val htmlContentTitle: String? = null,
    val htmlContentUrl: String? = null,
    val attemptsUrl: String? = null,
    val hasStarted: Boolean?,
    var coverImage: String? = null,
    val attachment: DomainAttachmentContent? = null,
    val htmlContent: DomainHtmlContent? = null,
    var exam: DomainExamContent? = null,
    val video: DomainVideoContent? = null,
    val videoConference: DomainVideoConferenceContent? = null,
    val isCourseAvailable: Boolean?,
    val coverImageSmall: String? = null,
    val coverImageMedium: String? = null,
    var nextContentId: Long? = null,
    val hasEnded: Boolean?,
    val examStartUrl: String? = null,
    val treePath: String? = null,
    val icon: String? = null
) {
    val contentTypeEnum: ContentType
        get() = contentType?.asEnumOrDefault(ContentType.Unknown)!!

    val isCourseNotPurchased = isCourseAvailable == false

    fun getFormattedStart(): String? {
        start?.let {
            val dateInMillis: Long = FormatDate.getDate(
                start,
                "yyyy-MM-dd'T'HH:mm:ss", "UTC"
            ).time
            return DateUtils.getRelativeTimeSpanString(dateInMillis).toString()
        }
        return null
    }

    fun hasAttempted(): Boolean {
        return (attemptsCount ?: 0) > 0
    }

    fun hasNotAttempted() = !hasAttempted()

    private fun canRetakeExam(): Boolean {
        if (exam?.allowRetake == true) {
            return (attemptsCount!! <= exam!!.maxRetakes!!) || (exam!!.maxRetakes == -1)
        }

        return false
    }

    fun canAttemptExam(): Boolean {
        if (exam == null) {
            return false
        }

        if (exam!!.isEnded() || exam!!.isWebOnly()) {
            return false
        }

        return canRetakeExam() || hasNotAttempted()
    }

    fun canShowRecordedVideo(): Boolean {
        return video != null && videoConference?.showRecordedVideo == true
    }
}

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
        hasStarted = contentEntity.hasStarted,
        isCourseAvailable = contentEntity.isCourseAvailable,
        coverImage = contentEntity.coverImage,
        coverImageSmall = contentEntity.coverImageSmall,
        coverImageMedium = contentEntity.coverImageMedium,
        nextContentId = contentEntity.nextContentId,
        hasEnded = contentEntity.hasEnded,
        examStartUrl = contentEntity.examStartUrl
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
        attemptsUrl = content.attemptsUrl,
        videoConferenceID = content.videoConferenceId,
        videoConference = content.rawVideoConference?.asDomainContent(),
        isCourseAvailable = content.isCourseAvailable,
        coverImageMedium = content.coverImageMedium,
        coverImage = content.coverImage,
        coverImageSmall = content.coverImageSmall,
        nextContentId = content.nextContentId,
        hasEnded = content.hasEnded,
        examStartUrl = content.examStartUrl
    )
}

fun createDomainContent(content: RunningContentEntity): DomainContent {
    return DomainContent(
        id = content.id,
        order = content.order,
        chapterId = content.chapterId,
        freePreview = content.freePreview,
        title = content.title,
        courseId = content.courseId,
        examId = content.examId,
        videoId = content.videoId,
        attachmentId = content.attachmentId,
        contentType = content.contentType,
        start = getFormattedDateStringOrNull(content.start),
        end = getFormattedDateStringOrNull(content.end),
        treePath = content.treePath,
        icon = content.icon,
        isLocked = null,
        isScheduled = null,
        active = null,
        hasEnded = null,
        isCourseAvailable = null,
        hasStarted = null,
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

fun List<RunningContentEntity>.convertRunningContentsToDomainContents(): List<DomainContent>{
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

fun DomainContent.getGreenDaoContentAttempts(context: Context): List<CourseAttempt> {
    val courseAttemptDao = TestpressSDKDatabase.getCourseAttemptDao(context)
    return courseAttemptDao.queryBuilder()
        .where(CourseAttemptDao.Properties.ChapterContentId.eq(this.id), CourseAttemptDao.Properties.ObjectUrl.isNotNull).list()
}

enum class ContentType {
    Exam, Quiz, Video, Attachment, Notes, Unknown
}

inline fun <reified T : Enum<T>> String.asEnumOrDefault(defaultValue: T? = null): T? =
        enumValues<T>().firstOrNull { it.name.equals(this, ignoreCase = true) } ?: defaultValue