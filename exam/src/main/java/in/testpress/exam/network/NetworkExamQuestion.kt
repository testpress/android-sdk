package `in`.testpress.exam.network

import `in`.testpress.models.greendao.ExamQuestion

data class NetworkExamQuestion(
    val id: Long,
    val order: Int? = null,
    val questionId: Long? = null,
    val sectionId: Long? = null,
    var examId: Long? = null,
    var attemptId: Long? = null
)

fun NetworkExamQuestion.asGreenDaoModel(): ExamQuestion {
    return ExamQuestion(id, order, examId, attemptId, questionId)
}

fun List<NetworkExamQuestion>.asGreenDaoModels(): List<ExamQuestion> {
    return this.map {
        it.asGreenDaoModel()
    }
}