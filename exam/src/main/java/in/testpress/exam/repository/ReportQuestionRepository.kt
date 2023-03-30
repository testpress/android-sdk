package `in`.testpress.exam.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.models.ReportQuestionResponse
import `in`.testpress.network.Resource

class ReportQuestionRepository(private val apiClient: TestpressExamApiClient) {

    private var _reportQuestions: MutableLiveData<Resource<ReportQuestionResponse>> =
        MutableLiveData()
    val reportQuestions: LiveData<Resource<ReportQuestionResponse>>
        get() = _reportQuestions

    private var _submitReport: MutableLiveData<Resource<ReportQuestionResponse.ReportQuestion>> =
        MutableLiveData()
    val submitReport: LiveData<Resource<ReportQuestionResponse.ReportQuestion>>
        get() = _submitReport

    fun getReportQuestions(questionId: String) {
        apiClient.getReportQuestions(questionId)
            .enqueue(object : TestpressCallback<ReportQuestionResponse>() {
                override fun onSuccess(result: ReportQuestionResponse) {
                    _reportQuestions.postValue(Resource.success(result))
                }

                override fun onException(exception: TestpressException) {
                    _reportQuestions.postValue(Resource.error(exception, null))
                }

            })
    }

    fun submitReportQuestion(questionId: String, params: HashMap<String, Any>) {
        _submitReport.postValue(Resource.loading(null))
        apiClient.postReportQuestion(questionId, params)
            .enqueue(object : TestpressCallback<ReportQuestionResponse.ReportQuestion>() {
                override fun onSuccess(result: ReportQuestionResponse.ReportQuestion) {
                    _submitReport.postValue(Resource.success(result))
                }

                override fun onException(exception: TestpressException) {
                    _submitReport.postValue(Resource.error(exception, null))
                }

            })
    }
}