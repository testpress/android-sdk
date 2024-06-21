package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.asOfflineExam
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.exam.network.asRoomModels
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.Resource
import `in`.testpress.util.extension.isNotNull
import `in`.testpress.util.extension.isNotNullAndNotEmpty
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineExamRepository(val context: Context) {

    private val courseClient = CourseNetwork(context)
    private val database = TestpressDatabase.invoke(context)
    private val offlineExamDao = database.offlineExamDao()
    private val languageDao = database.languageDao()

    private val _downloadExamResult = MutableLiveData<Resource<Boolean>>()
    val downloadExamResult: LiveData<Resource<Boolean>> get() = _downloadExamResult

    fun downloadExam(contentId: Long) {
        _downloadExamResult.postValue(Resource.loading(null))
        courseClient.getNetworkContentWithId(contentId)
            .enqueue(object : TestpressCallback<NetworkContent>() {
                override fun onSuccess(result: NetworkContent) {
                    CoroutineScope(Dispatchers.IO).launch {
                        handleExamDownloadSuccess(result)
                    }
                }

                override fun onException(exception: TestpressException) {
                    handleDownloadError(exception)
                }
            })
    }

    private fun handleExamDownloadSuccess(result: NetworkContent) {
        val offlineExam = result.asOfflineExam()
        offlineExamDao.insert(offlineExam)
        if (offlineExam.id.isNotNull() && offlineExam.slug.isNotNullAndNotEmpty()) {
            downloadLanguages(offlineExam.id!!, offlineExam.slug!!)
        } else {
            handleDownloadError(Exception("Exam Id or Exam Slug is empty"))
        }
    }

    private fun downloadLanguages(examId: Long, examSlug: String) {
        courseClient.getLanguages(examSlug)
            .enqueue(object : TestpressCallback<TestpressApiResponse<NetworkLanguage>>() {
                override fun onSuccess(result: TestpressApiResponse<NetworkLanguage>) {
                    CoroutineScope(Dispatchers.IO).launch {
                        languageDao.insertAll(result.results.asRoomModels(examId))
                        _downloadExamResult.postValue(Resource.success(true))
                    }
                }

                override fun onException(exception: TestpressException) {
                    handleDownloadError(exception)
                }
            })
    }

    private fun handleDownloadError(exception: Exception) {
        _downloadExamResult.postValue(
            Resource.error(
                TestpressException.unexpectedError(exception), null
            )
        )
    }
}