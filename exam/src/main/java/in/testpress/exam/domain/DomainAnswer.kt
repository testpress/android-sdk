package `in`.testpress.exam.domain

import `in`.testpress.models.greendao.Answer

data class DomainAnswer(
    val id: Long,
    val textHtml: String? = null,
    val isCorrect: Boolean? = null,
    var questionId: Long? = null,
    val marks: String? = null
)

fun Answer.asDomainModel(): DomainAnswer {
    return DomainAnswer(
        id = id,
        textHtml = textHtml,
        isCorrect = isCorrect,
        questionId = questionId,
        marks = marks
    )
}

fun List<Answer>.asDomainModels(): List<DomainAnswer> {
    return this.map {
        it.asDomainModel()
    }
}