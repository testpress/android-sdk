package `in`.testpress.exam.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.models.ReportQuestionResponse
import `in`.testpress.network.Resource

class ReportQuestionRepository(private val apiClient: TestpressExamApiClient) {

    private var _reportQuestion: MutableLiveData<Resource<ReportQuestionResponse>> =
        MutableLiveData()
    val reportQuestion: LiveData<Resource<ReportQuestionResponse>>
        get() = _reportQuestion

    private var _submitReport: MutableLiveData<Resource<ReportQuestionResponse.ReportQuestion>> =
        MutableLiveData()
    val submitReport: LiveData<Resource<ReportQuestionResponse.ReportQuestion>>
        get() = _submitReport

    fun getReportQuestion(questionId: String) {
        apiClient.getReportQuestionResponse(questionId)
            .enqueue(object : TestpressCallback<ReportQuestionResponse>() {
                override fun onSuccess(result: ReportQuestionResponse) {
                    _reportQuestion.postValue(Resource.success(result))
                }

                override fun onException(exception: TestpressException) {
                    _reportQuestion.postValue(Resource.error(exception, null))
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