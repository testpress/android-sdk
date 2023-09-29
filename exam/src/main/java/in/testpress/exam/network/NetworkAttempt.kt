package `in`.testpress.exam.network

import `in`.testpress.exam.domain.DomainAttempt
import `in`.testpress.models.greendao.Attempt

data class NetworkAttempt(
    val id: Long,
    val url: String? = null,
    val date: String? = null,
    val totalQuestions: Int? = null,
    val score: String? = null,
    val rank: String? = null,
    val maxRank: String? = null,
    val reviewUrl: String? = null,
    val questionsUrl: String? = null,
    val correctCount: Int? = null,
    val incorrectCount: Int? = null,
    val lastStartedTime: String? = null,
    val remainingTime: String? = null,
    val timeTaken: String? = null,
    val state: String? = null,
    val percentile: String? = null,
    val speed: Int? = null,
    val accuracy: Int? = null,
    val percentage: String? = null,
    val sections: List<NetworkAttemptSection>? = arrayListOf(),
    val lastViewedQuestionId: Int? = null,
    val externalReviewUrl:String? = null,
    val reviewPdf:String? = null,
    val rankEnabled: Boolean? = null,
    val attemptType: Int? = null
)

fun createNetworkAttempt(attempt: NetworkAttempt): Attempt {
    val greenDaoAttempt = Attempt(
        attempt.url,
        attempt.id,
        attempt.date,
        attempt.totalQuestions,
        attempt.score,
        attempt.rank,
        attempt.maxRank,
        attempt.reviewUrl,
        attempt.questionsUrl,
        attempt.correctCount,
        attempt.incorrectCount,
        attempt.lastStartedTime,
        attempt.remainingTime,
        attempt.timeTaken,
        attempt.state,
        attempt.percentile,
        attempt.speed,
        attempt.accuracy,
        attempt.percentage,
        attempt.lastViewedQuestionId,
        attempt.externalReviewUrl,
        attempt.reviewPdf,
        attempt.rankEnabled,
        attempt.attemptType
    )
    greenDaoAttempt.sections = attempt.sections?.asGreenDaoModel()
    return greenDaoAttempt
}

fun NetworkAttempt.asDomainModel() = DomainAttempt(
    id = this.id,
    url = this.url,
    date = this.date,
    score = this.score,
    totalQuestions = this.totalQuestions,
    reviewPdfUrl = this.reviewPdf,
    reviewUrl = this.reviewUrl,
    questionsUrl = this.questionsUrl,
    percentile = this.percentile,
    correctCount = this.correctCount,
    exam = null,
    incorrectCount = this.incorrectCount,
    lastStartedTime = this.lastStartedTime,
    remainingTime = this.remainingTime,
    timeTaken = this.timeTaken,
    state = this.state,
    rank = this.rank,
    maxRank = this.maxRank,
    percentage = this.percentage,
    unanswered_count = null,
    totalBonus = null,
    rankEnabled = this.rankEnabled,
    sections = this.sections?.asDomainModels(),
    speed = this.speed,
    accuracy = this.accuracy,
    lastViewedQuestionId = this.lastViewedQuestionId,
    externalReviewUrl = this.externalReviewUrl,
    reviewPdf = this.reviewPdf,
    attemptType = this.attemptType
)

fun NetworkAttempt.asGreenDaoModel(): Attempt {
    return createNetworkAttempt(this)
}