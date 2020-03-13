package `in`.testpress.course.domain

import `in`.testpress.models.greendao.Exam
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

data class DomainExamContent(
    val id: Long,
    val title: String? = null,
    val duration: String? = null,
    val description: String? = null,
    val url: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val numberOfQuestions: Int? = null,
    val negativeMarks: String? = null,
    val markPerQuestion: String? = null,
    val templateType: Int? = null,
    val allowRetake: Boolean? = null,
    val maxRetakes: Int? = null,
    val enableRanks: Boolean? = null,
    val attemptsUrl: String? = null,
    val attemptsCount: Int? = null,
    val pausedAttemptsCount: Int? = 0,
    val allowPdf: Boolean? = null,
    val slug: String? = null,
    val variableMarkPerQuestion: Boolean? = null,
    val showAnswers: Boolean? = null,
    val commentsCount: Int? = 0,
    val deviceAccessControl: String? = null,
    val passPercentage: Int? = null,
    val showPercentile: Boolean? = null,
    val showScore: Boolean? = null,
    val sections: List<DomainSection> = arrayListOf(),
    var languages: List<DomainLanguage> = arrayListOf(),
    val isGrowthHackEnabled: Boolean? = null,
    val shareTextForSolutionUnlock: String? = null
) {
    fun formattedDate(inputString: String): String {
        var date: Date? = null
        val simpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        try {
            if (inputString.isNotEmpty()) {
                date = simpleDateFormat.parse(inputString)
                val dateformat = DateFormat.getDateInstance()
                return dateformat.format(date)
            }
        } catch (e: ParseException) {
        }
        return "forever"
    }

    fun formattedStartDate() = formattedDate(startDate ?: "")

    fun formattedEndData() = formattedDate(endDate ?: "")

    fun isWebOnly(): Boolean {
        return deviceAccessControl != null && deviceAccessControl == "web"
    }

    private fun canRetake(): Boolean {
        return allowRetake!! &&
            (attemptsCount!! <= maxRetakes!! || maxRetakes < 0)
    }

    private fun isEnded(): Boolean {
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

    fun canBeAttempted(): Boolean {
        if (isEnded()) {
            return false
        }
        return if (attemptsCount == 0 || canRetake()) {
            !isWebOnly()
        } else false
    }

    fun hasMultipleLanguages() = languages.size > 1
}

fun createDomainExamContent(exam: Exam): DomainExamContent {
    return DomainExamContent(
        id = exam.id,
        title = exam.title,
        duration = exam.duration,
        description = exam.description,
        url = exam.url,
        startDate = exam.startDate,
        endDate = exam.endDate,
        numberOfQuestions = exam.numberOfQuestions,
        negativeMarks = exam.negativeMarks,
        markPerQuestion = exam.markPerQuestion,
        templateType = exam.templateType,
        allowRetake = exam.allowRetake,
        maxRetakes = exam.maxRetakes,
        enableRanks = exam.enableRanks,
        attemptsUrl = exam.attemptsUrl,
        attemptsCount = exam.attemptsCount,
        pausedAttemptsCount = exam.pausedAttemptsCount,
        allowPdf = exam.allowPdf,
        slug = exam.slug,
        variableMarkPerQuestion = exam.variableMarkPerQuestion,
        showAnswers = exam.showAnswers,
        commentsCount = exam.commentsCount,
        deviceAccessControl = exam.deviceAccessControl,
        passPercentage = exam.passPercentage,
        showPercentile = exam.showPercentile,
        showScore = exam.showScore,
        languages = exam.rawLanguages.asDomainLanguages()
    )
}

fun Exam.asDomainContent(): DomainExamContent {
    return createDomainExamContent(this)
}

fun createGreenDaoExamContent(exam: DomainExamContent): Exam {
    val greenDaoexam = Exam(
        null,
        exam.url,
        exam.id,
        exam.attemptsCount,
        exam.pausedAttemptsCount,
        exam.title,
        exam.description,
        exam.startDate,
        exam.endDate,
        exam.duration,
        exam.numberOfQuestions,
        exam.negativeMarks,
        exam.markPerQuestion,
        exam.templateType,
        exam.allowRetake,
        exam.allowPdf,
        exam.showAnswers,
        exam.maxRetakes,
        exam.attemptsUrl,
        exam.deviceAccessControl,
        exam.commentsCount,
        exam.slug,
        null,
        exam.variableMarkPerQuestion,
        exam.passPercentage,
        exam.enableRanks,
        exam.showScore,
        exam.showPercentile,
        null,
        null,
        exam.isGrowthHackEnabled,
        exam.shareTextForSolutionUnlock
    )
    greenDaoexam.languages = exam.languages.toGreenDaoModels()

    return greenDaoexam
}

fun DomainExamContent.asGreenDaoModel(): Exam {
    return createGreenDaoExamContent(this)
}