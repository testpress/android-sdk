package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.NetworkOfflineQuestionResponse
import `in`.testpress.course.network.asOfflineExam
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.exam.network.asRoomModels
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.Resource
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineExamRepository(val context: Context) {

    private val courseClient = CourseNetwork(context)
    private val database = TestpressDatabase.invoke(context)
    private var offlineExamDao = database.offlineExamDao()
    private var languageDao = database.languageDao()

    private val _downloadExamResult = MutableLiveData<Resource<Boolean>>()
    val downloadExamResult: LiveData<Resource<Boolean>> get() = _downloadExamResult

    private val _downloadQuestionsResult = MutableLiveData<Resource<Boolean>>()
    val downloadQuestionsResult: LiveData<Resource<Boolean>> get() = _downloadQuestionsResult

    private val _downloadLanguagesResult = MutableLiveData<Resource<Boolean>>()
    val downloadLanguagesResult: LiveData<Resource<Boolean>> get() = _downloadLanguagesResult

    fun downloadExam(contentId: Long) {
        courseClient.getNetworkContentWithId(contentId)
            .enqueue(object : TestpressCallback<NetworkContent>() {
                override fun onSuccess(result: NetworkContent) {
                    CoroutineScope(Dispatchers.IO).launch {
                        offlineExamDao.insert(result.asOfflineExam())
                        _downloadExamResult.postValue(Resource.success(true))
                    }
                }

                override fun onException(exception: TestpressException) {
                    _downloadExamResult.postValue(Resource.error(exception, null))
                }
            })
    }

    fun downloadQuestions(examId: Long) {
        var page = 1

        fun fetchQuestionsPage() {
            val queryParams = hashMapOf<String, Any>("page" to page)
            courseClient.getQuestions(examId, queryParams)
                .enqueue(object : TestpressCallback<ApiResponse<NetworkOfflineQuestionResponse>>() {
                    override fun onSuccess(result: ApiResponse<NetworkOfflineQuestionResponse>) {
                        if (result.next != null) {
                            // Save questions in db
                            page++
                            fetchQuestionsPage()
                        } else {
                            // Save questions in db
                            _downloadQuestionsResult.postValue(Resource.success(true))
                        }
                    }

                    override fun onException(exception: TestpressException) {
                        _downloadQuestionsResult.postValue(Resource.error(exception, null))
                    }
                })
        }

        fetchQuestionsPage()
    }

    fun downloadLanguages(examId: Long, examSlug: String) {
        courseClient.getLanguages(examSlug)
            .enqueue(object : TestpressCallback<TestpressApiResponse<NetworkLanguage>>() {
                override fun onSuccess(result: TestpressApiResponse<NetworkLanguage>) {
                    CoroutineScope(Dispatchers.IO).launch {
                        languageDao.insertAll(result.results.asRoomModels(examId))
                        _downloadLanguagesResult.postValue(Resource.success(true))
                    }
                }

                override fun onException(exception: TestpressException) {
                    _downloadLanguagesResult.postValue(Resource.error(exception, null))
                }
            })
    }

    suspend fun isExamDownloaded(examId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val offlineExam = offlineExamDao.get(examId)
            offlineExam != null
        }
    }

    fun getAllOfflineExams(): LiveData<List<OfflineExam>> {
        return offlineExamDao.getAll()
    }

    fun deleteExam(examId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            offlineExamDao.deleteById(examId)
        }
    }
}