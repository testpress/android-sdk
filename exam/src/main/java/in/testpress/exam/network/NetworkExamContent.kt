package `in`.testpress.exam.network

import `in`.testpress.exam.network.NetworkSection
import `in`.testpress.models.greendao.Exam
import java.text.SimpleDateFormat
import java.util.*

data class NetworkExamContent(
    val id: Long,
    val title: String = "",
    val duration: String? = null,
    val description: String = "",
    val url: String? = null,
    val startDate: String = "",
    val endDate: String? = null,
    val numberOfQuestions: Int = 0,
    val negativeMarks: String? = null,
    val markPerQuestion: String? = null,
    val templateType: Int? = null,
    val allowRetake: Boolean,
    val maxRetakes: Int? = null,
    val enableRanks: Boolean,
    val rankPublishingDate: String? = null,
    val attemptsUrl: String? = null,
    val attemptsCount: Int? = null,
    val pausedAttemptsCount: Int = 0,
    val allowPdf: Boolean? = null,
    val allowQuestionsPdf: Boolean? = null,
    val created: String = "",
    val slug: String = "",
    val variableMarkPerQuestion: Boolean,
    val showAnswers: Boolean,
    val commentsCount: Int = 0,
    val allowPreemptiveSectionEnding: Boolean? = null,
    val immediateFeedback: Boolean = false,
    val deviceAccessControl: String = "",
    val instructions: String? = null,
    val passPercentage: Int = 50,
    val showPercentile: Boolean = true,
    val showScore: Boolean = true,
    val studentsAttemptedCount: Int = 0,
    val customRedirectUrl: String = "",
    val overallStudentsAttemptedCount: Int = 0,
    val restrictRetakeDuration: Int? = null,
    val sections: List<NetworkSection> = arrayListOf(),
    val languages: List<String> = arrayListOf(),
    val isGrowthHackEnabled: Boolean? = null,
    val shareTextForSolutionUnlock: String? = null,
    val showAnalytics: Boolean? = null,
    val totalMarks: String? = null,
    val hasAudioQuestions: Boolean? = null,
    val enableQuizMode: Boolean? = null,
    val disableAttemptResume: Boolean? = null,
    val examDataModifiedOn: String? = null,
    val isOfflineExam: Boolean = false,
    val graceDurationForOfflineSubmission: Long? = null
)

fun NetworkExamContent.asGreenDaoModel(): Exam {
    return Exam(
        totalMarks,
        this.url,
        this.id,
        this.attemptsCount,
        this.pausedAttemptsCount,
        this.title,
        this.description,
        this.startDate,
        this.endDate,
        this.duration,
        this.numberOfQuestions,
        this.negativeMarks,
        this.markPerQuestion,
        this.templateType,
        this.allowRetake,
        this.allowPdf,
        this.showAnswers,
        this.maxRetakes,
        this.attemptsUrl,
        this.deviceAccessControl,
        this.commentsCount,
        this.slug,
        null,
        this.variableMarkPerQuestion,
        this.passPercentage,
        this.enableRanks,
        this.showScore,
        this.showPercentile,
        null,
        null,
        this.isGrowthHackEnabled,
        this.shareTextForSolutionUnlock,
        this.showAnalytics,
        this.instructions,
        this.hasAudioQuestions,
        this.rankPublishingDate,
        this.enableQuizMode,
        this.disableAttemptResume,
        this.allowPreemptiveSectionEnding,
        this.examDataModifiedOn,
        this.isOfflineExam,
        this.graceDurationForOfflineSubmission
    )
}

fun NetworkExamContent.getLastModifiedAsDate(): Date? {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
    return examDataModifiedOn?.let { dateFormat.parse(it) }
}
