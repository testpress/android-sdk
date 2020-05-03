package `in`.testpress.exam.network

import `in`.testpress.models.greendao.Answer

data class NetworkAnswer(
    val id: Long,
    val textHtml: String? = null,
    val isCorrect: Boolean? = null,
    var questionId: Long? = null
)


fun NetworkAnswer.asGreenDaoModel(): Answer {
    return Answer(id, textHtml, isCorrect, null, questionId)
}

fun List<NetworkAnswer>.asGreenDaoModels(): List<Answer> {
    return this.map {
        it.asGreenDaoModel()
    }
}