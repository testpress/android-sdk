package `in`.testpress.exam.network

import `in`.testpress.models.greendao.UserSelectedAnswer
import `in`.testpress.util.IntegerList

data class NetworkUserSelectedAnswer(
    val id: Long? = null,
    val order: Int? = null,
    val question: NetworkQuestion? = null,
    val review: Boolean? = null,
    val selectedAnswers: List<Int>? = null,
    val shortText: String? = null,
    val correctAnswers: List<NetworkAnswer>? = null,
    val explanationHtml: String? = null,
    val url: String? = null,
    var examId: Long? = null,
    var attemptId: Long? = null,
    var questionId: Long? = null,
    val duration: String? = null
)

fun NetworkUserSelectedAnswer.asGreenDaoModel(): UserSelectedAnswer {
    val selectedAnswersIntegerList = IntegerList()
    selectedAnswersIntegerList.addAll(selectedAnswers ?: listOf())
    val correctAnswersIds = IntegerList()
    correctAnswersIds.addAll(correctAnswers?.map {it.id.toInt()} ?: listOf())

    return UserSelectedAnswer(
        id, order, review, examId, attemptId, explanationHtml, shortText, duration,
        selectedAnswersIntegerList, correctAnswersIds, url, questionId
    )
}