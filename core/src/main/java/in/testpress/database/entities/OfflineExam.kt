package `in`.testpress.database.entities

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class OfflineExam(
    val id: Long? = null,
    val totalMarks: String? = null,
    val url: String? = null,
    val attemptsCount: Int? = null,
    val pausedAttemptsCount: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val duration: String? = null,
    val numberOfQuestions: Int? = null,
    val negativeMarks: String? = null,
    val markPerQuestion: String? = null,
    val templateType: Int? = null,
    val allowRetake: Boolean? = null,
    val allowPdf: Boolean? = null,
    val showAnswers: Boolean? = null,
    val maxRetakes: Int? = null,
    val attemptsUrl: String? = null,
    val deviceAccessControl: String? = null,
    val commentsCount: Int? = null,
    val slug: String? = null,
    val selectedLanguage: String? = null,
    val variableMarkPerQuestion: Boolean? = null,
    val passPercentage: Int? = null,
    val enableRanks: Boolean? = null,
    val showScore: Boolean? = null,
    val showPercentile: Boolean? = null,
    val categories: List<String>? = null,
    val isDetailsFetched: Boolean? = null,
    val isGrowthHackEnabled: Boolean? = null,
    val shareTextForSolutionUnlock: String? = null,
    val showAnalytics: Boolean? = null,
    val instructions: String? = null,
    val hasAudioQuestions: Boolean? = null,
    val rankPublishingDate: String? = null,
    val enableQuizMode: Boolean? = null,
    val disableAttemptResume: Boolean? = null,
    val allowPreemptiveSectionEnding: Boolean? = null,
    @Embedded
    val languages: List<OfflineLanguage>? = null
)