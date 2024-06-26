package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.NetworkOfflineQuestionResponse
import `in`.testpress.course.network.asOfflineExam
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.exam.network.NetworkExamContent
import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.exam.network.asRoomModels
import `in`.testpress.exam.network.getLastModifiedAsDate
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.Resource
import `in`.testpress.util.extension.isNotNull
import `in`.testpress.util.extension.isNotNullAndNotEmpty
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import android.widget.Toast
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
    private val directionDao = database.directionDao()
    private val subjectDao = database.subjectDao()
    private val sectionsDao = database.sectionsDao()
    private val examQuestionDao = database.examQuestionDao()
    private val questionDao = database.questionDao()

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
                        downloadQuestions(examId)
                    }
                }

                override fun onException(exception: TestpressException) {
                    handleDownloadError(exception)
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
                            saveQuestionsToDB(result.results)
                            page++
                            fetchQuestionsPage()
                        } else {
                            saveQuestionsToDB(result.results)
                            _downloadExamResult.postValue(Resource.success(true))
                        }
                    }

                    override fun onException(exception: TestpressException) {
                        handleDownloadError(exception)
                    }
                })
        }

        fetchQuestionsPage()
    }

    private fun saveQuestionsToDB(response: NetworkOfflineQuestionResponse){
        CoroutineScope(Dispatchers.IO).launch {
            directionDao.insertAll(response.directions)
            subjectDao.insertAll(response.subjects)
            sectionsDao.insertAll(response.sections)
            examQuestionDao.insertAll(response.examQuestions)
            questionDao.insertAll(response.questions)
        }
    }


    private fun handleDownloadError(exception: Exception) {
        _downloadExamResult.postValue(
            Resource.error(
                TestpressException.unexpectedError(exception), null
            )
        )
    }

    fun getAll():LiveData<List<OfflineExam>>{
        return offlineExamDao.getAll()
    }

    suspend fun deleteOfflineExam(examId: Long) {
        offlineExamDao.deleteById(examId)
        examQuestionDao.deleteByExamId(examId)
        // Here we are deleting exam and exam question only
        // Deleting Question, Direction, Section, Subject need to handle
    }

    suspend fun syncExamsModifiedDates() {
        var page = 1
        val examIds = offlineExamDao.getAllIds()
        if (examIds.isEmpty()) return
        val examModifications = mutableListOf<NetworkExamContent>()

        fun fetchExamModifiedDate() {
            val queryParams =
                hashMapOf<String, Any>("page" to page, "id" to examIds.joinToString(","))
            courseClient.getExams(queryParams)
                .enqueue(object : TestpressCallback<ApiResponse<List<NetworkExamContent>>>() {
                    override fun onSuccess(result: ApiResponse<List<NetworkExamContent>>) {
                        if (result.next != null) {
                            examModifications.addAll(result.results)
                            page++
                            fetchExamModifiedDate()
                        } else {
                            examModifications.addAll(result.results)
                            CoroutineScope(Dispatchers.IO).launch {
                                updateSyncStatus(examModifications)
                            }
                        }
                    }

                    override fun onException(exception: TestpressException) {
                        when {
                            exception.isNetworkError -> Toast.makeText(
                                context,
                                "Please check your internet connection",
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> Toast.makeText(
                                context,
                                "Please check your internet connection",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        }

        fetchExamModifiedDate()
    }

    private suspend fun updateSyncStatus(networkExamList: List<NetworkExamContent>) {
        networkExamList.forEach { networkExam ->
            val exam = offlineExamDao.getById(networkExam.id)
            if (exam?.getExamDataModifiedOnAsDate()?.before(networkExam.getLastModifiedAsDate()) == true) {
                offlineExamDao.updateSyncRequired(networkExam.id, true)
            }
        }
    }
}