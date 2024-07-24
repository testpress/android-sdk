package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class OfflineExam(
    @PrimaryKey
    val id: Long? = null,
    val totalMarks: String? = null,
    val url: String? = null,
    val attemptsCount: Int? = null,
    var pausedAttemptsCount: Int? = null,
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
    val examDataModifiedOn: String? = null,
    var isSyncRequired: Boolean = false,
    val contentId: Long? = null,
    val downloadedQuestionCount: Long = 0,
    val downloadComplete: Boolean = false
) {
    fun getExamDataModifiedOnAsDate(): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
        return examDataModifiedOn?.let { dateFormat.parse(it) }
    }

    fun getDownloadProgress(): Int {
        if (numberOfQuestions != null && numberOfQuestions > 0) {
            return ((downloadedQuestionCount * 100) / numberOfQuestions).toInt()
        }
        return 0
    }

    fun canAttemptExam(): Boolean {
        if (isEnded() || isWebOnly()) {
            return false
        }
        return canRetakeExam() || hasNotAttempted()
    }

    fun isEnded(): Boolean {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        try {
            if(!endDate.isNullOrEmpty()) {
                return simpleDateFormat.parse(endDate).before(Date())
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    private fun isWebOnly(): Boolean {
        return deviceAccessControl != null && deviceAccessControl == "web"
    }

    private fun canRetakeExam(): Boolean {
        if (allowRetake == true) {
            return (attemptsCount!! <= maxRetakes!!) || (maxRetakes == -1)
        }
        return false
    }

    private fun hasNotAttempted() = !hasAttempted()

    private fun hasAttempted(): Boolean {
        return (attemptsCount ?: 0) > 0
    }
}