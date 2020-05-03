package `in`.testpress.exam.network

import `in`.testpress.models.greendao.Direction

data class NetworkExamQuestionResult(
    val directions: List<NetworkDirection>? = listOf(),
    val examQuestions: List<NetworkExamQuestion>? = listOf(),
    val questions: List<NetworkQuestion>? = listOf()
)

data class NetworkDirection(
    val id: Long,
    val html: String? = null
)

fun NetworkDirection.asGreenDaoModel(): Direction {
    return Direction(id, html)
}

fun List<NetworkDirection>.asGreenDaoModels(): List<Direction> {
    return this.map {
        it.asGreenDaoModel()
    }
}