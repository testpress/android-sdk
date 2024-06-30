package `in`.testpress.database.mapping

import `in`.testpress.database.entities.OfflineAttempt
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.AttemptSection

fun OfflineAttempt.createGreenDoaModel(attemptSections: List<AttemptSection>): Attempt {
    val attempt = Attempt()
    attempt.url = null
    attempt.id = this.id
    attempt.date = this.date
    attempt.totalQuestions = this.totalQuestions
    attempt.score = null
    attempt.rank = null
    attempt.maxRank = null
    attempt.reviewUrl = null
    attempt.questionsUrl = null
    attempt.correctCount = null
    attempt.incorrectCount = null
    attempt.lastStartedTime = this.lastStartedTime
    attempt.remainingTime = this.remainingTime
    attempt.timeTaken = this.timeTaken
    attempt.state = this.state
    attempt.percentile = null
    attempt.speed = null
    attempt.accuracy = null
    attempt.percentage = null
    attempt.lastViewedQuestionId = null
    attempt.externalReviewUrl = null
    attempt.reviewPdf = null
    attempt.rankEnabled = null
    attempt.attemptType = this.attemptType
    attempt.sections = attemptSections
    return attempt
}