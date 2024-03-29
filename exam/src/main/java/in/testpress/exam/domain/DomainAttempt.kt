package `in`.testpress.exam.domain

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.AttemptDao
import android.content.Context

data class DomainAttempt(
    val id : Long,
    val url : String?,
    val date : String? = null,
    val score : String? = null,
    val totalQuestions : Int? = null,
    val reviewPdfUrl : String? = null,
    val reviewUrl : String? = null,
    val questionsUrl : String? = null,
    val percentile : String? = null,
    val correctCount : Int? = null,
    val exam: DomainExamContent? = null,
    val incorrectCount : Int? = null,
    val lastStartedTime : String? = null,
    val remainingTime : String? = null,
    val timeTaken : String? = null,
    val state : String? = null,
    val rank : String? = null,
    val maxRank : String? = null,
    val percentage : String? = null,
    val unanswered_count : Int? = null,
    val totalBonus : Int? = null,
    val rankEnabled : Boolean? = null,
    val sections : List<DomainAttemptSection>? = null,
    val speed : Int? = null,
    val accuracy : Int? = null,
    val lastViewedQuestionId: Int? = null,
    val externalReviewUrl :String? = null,
    val reviewPdf: String? = null,
    val attemptType: Int? = null
) {
    val endUrl = url + "end/"
    fun isAttemptTypeQuiz() = attemptType != null && attemptType == 1
}

fun Attempt.asDomainModel(): DomainAttempt {
    return DomainAttempt(
        id = id,
        url = url,
        date = date,
        totalQuestions = totalQuestions,
        score = score,
        rank = rank,
        maxRank = maxRank,
        reviewUrl = reviewUrl,
        questionsUrl = questionsUrl,
        correctCount = correctCount,
        incorrectCount = incorrectCount,
        lastStartedTime = lastStartedTime,
        remainingTime = remainingTime,
        timeTaken = timeTaken,
        state = state,
        percentile = percentile,
        speed = speed,
        accuracy = accuracy,
        percentage = percentage,
        lastViewedQuestionId = lastViewedQuestionId,
        externalReviewUrl = externalReviewUrl,
        reviewPdf = reviewPdf,
        rankEnabled = rankEnabled,
        attemptType = attemptType,
        sections = sections.asDomainModels()
    )
}

fun DomainAttempt.getGreenDaoAttempt(context: Context): Attempt? {
    val attemptDao = TestpressSDKDatabase.getAttemptDao(context)
    val attempts =  attemptDao.queryBuilder().where(AttemptDao.Properties.Id.eq(this.id)).list()
    if (attempts.isNotEmpty()) {
        return attempts[0]
    }
    return null
}