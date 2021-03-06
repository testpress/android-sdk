package `in`.testpress.exam.network

import `in`.testpress.models.greendao.Question

data class NetworkQuestion(
    var id: Long? = null,
    val directionId: Long? = null,
    val answers: List<NetworkAnswer>? = listOf(),
    val language: String? = null,
    val questionHtml: String? = null,
    val subjectId: Long? = null,
    val parentId: Long? = null,
    val type: String? = null,
    val explanationHtml: String? = null,
    val direction: String? = null,
    val percentageGotCorrect: String? = null
)

fun NetworkQuestion.asGreenDaoModel(): Question {
    return Question(
        id,
        questionHtml,
        direction,
        parentId,
        type,
        language,
        explanationHtml,
        null,
        percentageGotCorrect,
        null,
        directionId
    )
}

fun List<NetworkQuestion>.asGreenDaoModels(): List<Question> {
    return this.map {
        it.asGreenDaoModel()
    }
}
