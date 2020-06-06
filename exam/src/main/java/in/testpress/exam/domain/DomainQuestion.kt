package `in`.testpress.exam.domain

import `in`.testpress.models.greendao.Question

data class DomainQuestion(
    var id: Long? = null,
    val directionId: Long? = null,
    val answers: List<DomainAnswer>? = listOf(),
    val language: String? = null,
    val questionHtml: String? = null,
    val subjectId: Long? = null,
    val parentId: Long? = null,
    val type: String? = null,
    val explanation: String? = null,
    val directionHtml: String? = null,
    val percentageGotCorrect: String? = null
) {
    val isSingleMCQType: Boolean = type == "R"
    val isMultipleMCQType: Boolean = type == "C"
    val isShortAnswerType: Boolean = type == "S"
    val isNumericalType: Boolean = type == "N"
}

fun Question.asDomainModel(): DomainQuestion {
    val directionHtml = direction?.html ?: directionHtml
    return DomainQuestion(
        id = id,
        directionId = directionId,
        answers = answers.asDomainModels(),
        language = language,
        questionHtml = questionHtml,
        subjectId = null,
        parentId = parentId,
        type = type,
        explanation = explanationHtml,
        directionHtml = directionHtml,
        percentageGotCorrect = percentageGotCorrect
    )
}