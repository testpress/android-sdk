package `in`.testpress.exam.domain

import `in`.testpress.models.greendao.UserSelectedAnswer

data class DomainUserSelectedAnswer(
    val id: Long? = null,
    val order: Int? = null,
    val question: DomainQuestion? = null,
    val review: Boolean? = null,
    val selectedAnswers: List<Int>? = null,
    val shortText: String? = null,
    val correctAnswers: List<Int>? = null,
    val explanationHtml: String? = null,
    val url: String? = null,
    var examId: Long? = null,
    var questionId: Long? = null
)

fun UserSelectedAnswer.asDomainModel(): DomainUserSelectedAnswer {
    return DomainUserSelectedAnswer(
        id = id,
        order = order,
        question = question.asDomainModel(),
        review = review,
        selectedAnswers = selectedAnswers,
        shortText = shortText,
        correctAnswers = correctAnswers,
        explanationHtml = explanationHtml,
        url = url,
        questionId = questionId
    )
}

fun List<UserSelectedAnswer>.asDomainModels(): List<DomainUserSelectedAnswer> {
    return this.map {
        it.asDomainModel()
    }
}
