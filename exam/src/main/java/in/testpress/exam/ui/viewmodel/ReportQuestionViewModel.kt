package `in`.testpress.exam.ui.viewmodel

import androidx.lifecycle.ViewModel
import `in`.testpress.exam.repository.ReportQuestionRepository

class ReportQuestionViewModel(
    private val reportQuestionRepository: ReportQuestionRepository,
    private val questionId: Long
) : ViewModel() {

    init {
        reportQuestionRepository.getReportQuestions(questionId.toString())
    }

    val questionReports = reportQuestionRepository.questionReports

    val submitReport = reportQuestionRepository.submitReport

    fun retry(){
        reportQuestionRepository.getReportQuestions(questionId.toString())
    }

    fun submitReportQuestion(
        description: String,
        examId: Long,
        type: Int
    ) {
        val params = HashMap<String, Any>()
        params["description"] = description
        params["type"] = type.toString()
        if (examId != -1L) {
            params["exam_id"] = examId.toString()
        }
        reportQuestionRepository.submitReportQuestion(questionId.toString(), params)
    }
}