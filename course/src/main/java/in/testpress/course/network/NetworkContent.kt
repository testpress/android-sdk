package `in`.testpress.course.network

import `in`.testpress.database.ContentEntity
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.exam.network.NetworkExamContent
import `in`.testpress.models.greendao.Content
import android.util.Log

data class NetworkContent(
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
    val videoWatchedPercentage: Int = 0,
    val active: Boolean,
    val examId: Long? = null,
    val attachmentId: Long? = null,
    val videoId: Long? = null,
    val htmlId: Long? = null,
    val videoConferenceId: Long? = null,
    val liveStreamId: Long? = null,
    val start: String? = null,
    val end: String? = null,
    val htmlContentTitle: String? = null,
    val htmlContentUrl: String? = null,
    val attemptsUrl: String? = null,
    val hasStarted: Boolean,
    val coverImage: String? = null,
    val coverImageSmall: String? = null,
    val coverImageMedium: String? = null,
    val exam: NetworkExamContent? = null,
    val htmlContent: NetworkHtmlContent? = null,
    val attachment: NetworkAttachmentContent? = null,
    val video: NetworkVideoContent? = null,
    val videoConference: NetworkVideoConferenceContent? = null,
    val liveStream: NetworkLiveStream? = null,
    val isCourseAvailable: Boolean? = null,
    val nextContentId: Long? = null,
    val hasEnded: Boolean? = null,
    val examStartUrl: String? = null
)

fun NetworkContent.asDatabaseModel(): ContentEntity {
    val contentEntity = ContentEntity(
        id = this.id,
        description = this.description,
        image = this.image,
        url = this.url,
        chapterSlug = this.chapterSlug,
        chapterUrl = this.chapterUrl,
        modified = this.modified,
        examUrl = this.examUrl,
        videoUrl = this.videoUrl,
        attachmentUrl = this.attachmentUrl,
        htmlUrl = this.htmlUrl,
        isLocked = this.isLocked,
        isScheduled = this.isScheduled,
        attemptsCount = this.attemptsCount,
        bookmarkId = this.bookmarkId,
        videoWatchedPercentage = this.videoWatchedPercentage,
        active = this.active,
        htmlId = this.htmlId,
        hasStarted = this.hasStarted,
        isCourseAvailable = this.isCourseAvailable,
        coverImage = this.coverImage,
        coverImageMedium = this.coverImageMedium,
        coverImageSmall = this.coverImageSmall,
        nextContentId = this.nextContentId,
        hasEnded = this.hasEnded,
        examStartUrl = this.examStartUrl
    )
    contentEntity.title = this.title
    contentEntity.order = this.order
    contentEntity.chapterId = this.chapterId
    contentEntity.courseId = this.courseId
    contentEntity.freePreview  = this.freePreview
    contentEntity.contentType = this.contentType
    contentEntity.examId = this.examId
    contentEntity.attachmentId = this.attachmentId
    contentEntity.videoId = this.videoId
    contentEntity.liveStreamId = this.liveStreamId
    contentEntity.start = this.start
    return contentEntity
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
        this.coverImageMedium,
        this.coverImageSmall,
        this.isCourseAvailable,
        this.nextContentId,
        this.hasEnded,
        this.examStartUrl,
        this.courseId,
        this.chapterId,
        this.videoConferenceId,
        this.liveStreamId,
        this.htmlId,
        this.videoId,
        this.attachmentId,
        this.examId
    )
}

fun NetworkContent.asOfflineExam(): OfflineExam {
    return OfflineExam(
        this.exam?.id,
        this.exam?.totalMarks,
        this.exam?.url,
        this.exam?.attemptsCount,
        this.exam?.pausedAttemptsCount,
        this.exam?.title,
        this.exam?.description,
        this.exam?.startDate,
        this.exam?.endDate,
        this.exam?.duration,
        this.exam?.numberOfQuestions,
        this.exam?.negativeMarks,
        this.exam?.markPerQuestion,
        this.exam?.templateType,
        this.exam?.allowRetake,
        this.exam?.allowPdf,
        this.exam?.showAnswers,
        this.exam?.maxRetakes,
        this.exam?.attemptsUrl,
        this.exam?.deviceAccessControl,
        this.exam?.commentsCount,
        this.exam?.slug,
        selectedLanguage = null,
        this.exam?.variableMarkPerQuestion,
        this.exam?.passPercentage,
        this.exam?.enableRanks,
        this.exam?.showScore,
        this.exam?.showPercentile,
        categories = null,
        isDetailsFetched = null,
        this.exam?.isGrowthHackEnabled,
        this.exam?.shareTextForSolutionUnlock,
        this.exam?.showAnalytics,
        this.exam?.instructions,
        this.exam?.hasAudioQuestions,
        this.exam?.rankPublishingDate,
        this.exam?.enableQuizMode,
        this.exam?.disableAttemptResume,
        this.exam?.allowPreemptiveSectionEnding,
        examDataModifiedOn = null
    )
}
