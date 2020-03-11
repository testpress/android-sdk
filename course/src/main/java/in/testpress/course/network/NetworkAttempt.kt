package `in`.testpress.course.network

import `in`.testpress.database.AttemptEntity
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
    val percentage: String? = null
)

fun createNetworkAttempt(attempt: NetworkAttempt): Attempt {
    return Attempt(
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
        attempt.percentage
    )
}

fun createAttemptEntity(attempt: NetworkAttempt): AttemptEntity {
    return AttemptEntity(
        attempt.id,
        attempt.url,
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
        attempt.percentage
    )
}

fun NetworkAttempt.asGreenDaoModel(): Attempt {
    return createNetworkAttempt(this)
}

fun NetworkAttempt.asDatabaseModel(): AttemptEntity {
    return createAttemptEntity(this)
}