package `in`.testpress.exam.ui.viewmodel

import androidx.lifecycle.ViewModel
import `in`.testpress.exam.repository.ReportQuestionRepository

class ReportQuestionViewModel(
    private val reportQuestionRepository: ReportQuestionRepository,
    private val questionId: String
) : ViewModel() {

    init {
        reportQuestionRepository.getReportQuestion(questionId)
    }

    val reportQuestion = reportQuestionRepository.reportQuestion

    val submitReport = reportQuestionRepository.submitReport

    fun retry(){
        reportQuestionRepository.getReportQuestion(questionId)
    }

    fun submitReportQuestion(
        description: String,
        examId: String,
        type: String
    ) {
        val params = HashMap<String, Any>()
        params["description"] = description
        params["exam_id"] = examId
        params["type"] = type
        reportQuestionRepository.submitReportQuestion(questionId, params)
    }
}