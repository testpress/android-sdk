package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.NetworkOfflineQuestionResponse
import `in`.testpress.course.network.asOfflineExam
import `in`.testpress.course.util.ResourcesDownloader
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.*
import `in`.testpress.database.mapping.asGreenDaoModel
import `in`.testpress.database.mapping.asGreenDoaModels
import `in`.testpress.database.mapping.createGreenDoaModel
import `in`.testpress.exam.network.NetworkExamContent
import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.exam.network.asRoomModels
import `in`.testpress.exam.network.getLastModifiedAsDate
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.util.PagedApiFetcher
import `in`.testpress.util.extension.isNotNull
import `in`.testpress.util.extension.isNotNullAndNotEmpty
import `in`.testpress.util.extension.validateHttpAndHttpsUrls
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

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
    private val offlineAttemptDao = database.offlineAttemptDao()
    private val offlineCourseAttemptDao = database.offlineCourseAttemptDao()
    private val offlineAttemptSectionDao = database.offlineAttemptSectionDao()
    private val offlineAttemptItemDao = database.offlineAttemptItemDoa()
    private val directions = mutableListOf<Direction>()
    private val subjects = mutableListOf<Subject>()
    private val sections = mutableListOf<Section>()
    private val examQuestions = mutableListOf<ExamQuestion>()
    private val questions = mutableListOf<Question>()

    private val _downloadExamResult = MutableLiveData<Resource<Boolean>>()
    val downloadExamResult: LiveData<Resource<Boolean>> get() = _downloadExamResult

    private val _offlineAttemptSyncResult = MutableLiveData<Resource<Boolean>>()
    val offlineAttemptSyncResult: LiveData<Resource<Boolean>> get() = _offlineAttemptSyncResult

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
        CoroutineScope(Dispatchers.IO).launch {
            val offlineExam = result.asOfflineExam()
            val pausedAttemptCount = offlineAttemptDao.getOfflineAttemptsByExamIdAndState(offlineExam.id!!,Attempt.RUNNING).size ?:0
            offlineExam.pausedAttemptsCount = pausedAttemptCount
            offlineExamDao.insert(offlineExam)
            if (offlineExam.id.isNotNull() && offlineExam.slug.isNotNullAndNotEmpty()) {
                downloadLanguages(offlineExam.id!!, offlineExam.slug!!)
            } else {
                handleDownloadError(Exception("Exam Id or Exam Slug is empty"))
            }
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
                            handleSuccessResponse(result.results)
                            updateOfflineExamDownloadPercent(examId, result.results!!.questions.size.toLong())
                            page++
                            fetchQuestionsPage()
                        } else {
                            handleSuccessResponse(result.results, lastPage = true)
                            updateOfflineExamDownloadPercent(examId, result.results!!.questions.size.toLong())
                            updateDownloadedState(examId)
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

    private fun updateOfflineExamDownloadPercent(examId: Long, count: Long){
        CoroutineScope(Dispatchers.IO).launch {
            offlineExamDao.updateDownloadedQuestion(examId, count)
        }
    }

    private fun updateDownloadedState(examId: Long){
        CoroutineScope(Dispatchers.IO).launch {
            offlineExamDao.updateDownloadedState(examId, true)
        }
    }

    fun getOfflineAttemptsByCompleteState() :LiveData<List<OfflineAttempt>> {
        return offlineAttemptDao.getOfflineAttemptsByCompleteState()
    }

    private fun handleSuccessResponse(
        response: NetworkOfflineQuestionResponse,
        lastPage: Boolean = false
    ) {
        directions.addAll(response.directions)
        subjects.addAll(response.subjects)
        sections.addAll(response.sections)
        examQuestions.addAll(response.examQuestions)
        questions.addAll(response.questions)

        if (lastPage) {
            CoroutineScope(Dispatchers.IO).launch {
                val result = NetworkOfflineQuestionResponse(
                    directions,
                    subjects,
                    sections,
                    examQuestions,
                    questions,
                )

                val examResourcesUrl =
                    result.extractUrls().toSet().toList().validateHttpAndHttpsUrls()

                ResourcesDownloader(context).downloadResources(examResourcesUrl) { urlToLocalPaths ->
                    result.replaceResourceUrlWithLocalUrl(urlToLocalPaths)
                    directionDao.insertAll(result.directions)
                    subjectDao.insertAll(result.subjects)
                    sectionsDao.insertAll(result.sections)
                    examQuestionDao.insertAll(result.examQuestions)
                    questionDao.insertAll(result.questions)
                    _downloadExamResult.postValue(Resource.success(true))
                }
            }
        }
    }


    private fun handleDownloadError(exception: Exception) {
        _downloadExamResult.postValue(
            Resource.error(
                TestpressException.unexpectedError(exception), null
            )
        )
    }

    fun get(contentId: Long):LiveData<OfflineExam?>{
        return offlineExamDao.get(contentId)
    }

    fun getAll():LiveData<List<OfflineExam>>{
        return offlineExamDao.getAll()
    }

    suspend fun deleteOfflineExam(examId: Long) {
        offlineExamDao.deleteById(examId)
        examQuestionDao.deleteByExamId(examId)
        val attemptIds = offlineAttemptDao.getAttemptIdsByExamId(examId)
        attemptIds.forEach { attemptId ->
            offlineAttemptDao.deleteByAttemptId(attemptId)
            offlineAttemptSectionDao.deleteByAttemptId(attemptId)
            offlineAttemptItemDao.deleteByAttemptId(attemptId)
            offlineCourseAttemptDao.deleteByAttemptId(attemptId)
        }
        // Here we are deleting exam and exam question only
        // Deleting Question, Direction, Section, Subject need to handle
    }

    suspend fun syncExamsModifiedDates() {
        val examIds = offlineExamDao.getAllIds()
        if (examIds.isEmpty()) return
        val examModifications = mutableListOf<NetworkExamContent>()

        val pagedApiFetcher = object : PagedApiFetcher<List<NetworkExamContent>>() {
            override fun createApiCall(page: Int): RetrofitCall<ApiResponse<List<NetworkExamContent>>> {
                val queryParams =
                    hashMapOf<String, Any>("page" to page, "id" to examIds.joinToString(","))
                return courseClient.getExams(queryParams)
            }

            override fun handlePageResults(results: List<NetworkExamContent>) {
                examModifications.addAll(results)
            }

            override fun onAllPagesFetched() {
                CoroutineScope(Dispatchers.IO).launch {
                    updateSyncStatus(examModifications)
                }
            }

            override fun onFetchError(exception: TestpressException) {
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
        }

        pagedApiFetcher.fetchAllPages()
    }

    private suspend fun updateSyncStatus(networkExamList: List<NetworkExamContent>) {
        for (networkExam in networkExamList) {
            val exam = offlineExamDao.getById(networkExam.id)
            if (exam?.getExamDataModifiedOnAsDate() == null || networkExam.getLastModifiedAsDate() == null) continue
            if (exam.getExamDataModifiedOnAsDate()?.before(networkExam.getLastModifiedAsDate()) == true) {
                offlineExamDao.updateSyncRequired(networkExam.id, true)
            }
        }
    }

    suspend fun getOfflineAttemptsByExamIdAndState(examId: Long, state: String): List<OfflineAttempt> {
        return offlineAttemptDao.getOfflineAttemptsByExamIdAndState(examId,state)
    }

    suspend fun getOfflineContentAttempts(attemptId: Long): OfflineCourseAttempt? {
        return offlineCourseAttemptDao.getById(attemptId)
    }

    suspend fun getOfflineAttemptSectionList(attemptId: Long): List<OfflineAttemptSection> {
        return offlineAttemptSectionDao.getByAttemptId(attemptId)
    }

    suspend fun syncCompletedAttempt(examId: Long){
        val completedOfflineAttempts = offlineAttemptDao.getOfflineAttemptsByExamIdAndState(examId, Attempt.COMPLETED)
        updateCompletedAttempts(completedOfflineAttempts)
    }

    suspend fun syncCompletedAllAttemptToBackEnd() {
        val completedOfflineAttempts =
            offlineAttemptDao.getOfflineAttemptsByState(Attempt.COMPLETED)
        updateCompletedAttempts(completedOfflineAttempts)
    }

    private suspend fun updateCompletedAttempts(completedOfflineAttempts: List<OfflineAttempt>){
        if(completedOfflineAttempts.isEmpty()) return
        val totalAttempts = completedOfflineAttempts.size
        var currentAttemptSize = 0
        completedOfflineAttempts.forEach { completedOfflineAttempt ->

            val attemptItems =
                offlineAttemptItemDao.getOfflineAttemptItemByAttemptId(completedOfflineAttempt.id)

            val attemptAnswers = attemptItems.map { attemptItem ->
                OfflineAnswer(
                    examQuestionId = examQuestionDao.getExamQuestionIdByExamIdAndQuestionId(
                        completedOfflineAttempt.examId,
                        attemptItem.question.id!!
                    ),
                    duration = null,
                    selectedAnswers = attemptItem.savedAnswers,
                    essayText = attemptItem.essayText,
                    shortText = attemptItem.shortText,
                    files = null,
                    gapFillResponses = null
                )
            }

            val offlineAttempt = OfflineAttemptDetail(
                chapterContentId = offlineExamDao.getContentIdByExamId(completedOfflineAttempt.examId),
                startedOn = convertDateStringToISO8601(completedOfflineAttempt.date),
                completedOn = convertDateStringToISO8601(completedOfflineAttempt.lastStartedTime)
            )

            courseClient.updateOfflineAnswers(
                completedOfflineAttempt.examId,
                offlineAttempt,
                attemptAnswers
            ).enqueue(object : TestpressCallback<HashMap<String, String>>() {
                override fun onSuccess(result: HashMap<String, String>) {
                    if (result["message"] == "Exam answers are being processed") {
                        deleteSyncedAttempt(completedOfflineAttempt.id)
                        currentAttemptSize++
                        if (totalAttempts == currentAttemptSize){
                            _offlineAttemptSyncResult.postValue(Resource.success(true))
                        }
                    }
                }

                override fun onException(exception: TestpressException) {
                    Log.e("OfflineExamRepository", "Failed to update offline answers", exception)
                    currentAttemptSize++
                    if (totalAttempts == currentAttemptSize){
                        _offlineAttemptSyncResult.postValue(Resource.error(exception, null))
                    }
                }
            })
        }
    }

    private fun deleteSyncedAttempt(attemptId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            offlineAttemptDao.deleteByAttemptId(attemptId)
            offlineAttemptSectionDao.deleteByAttemptId(attemptId)
            offlineAttemptItemDao.deleteByAttemptId(attemptId)
            offlineCourseAttemptDao.deleteByAttemptId(attemptId)
        }
    }

    private fun convertDateStringToISO8601(dateString: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val zonedDateTime = ZonedDateTime.parse(dateString, inputFormatter)
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        return utcDateTime.format(outputFormatter)
    }

    suspend fun getOfflineExamContent(contentId: Long): Content? {
        getContentFromDB(contentId)?.let { content ->
            offlineExamDao.getByContentId(contentId)?.let { offlineExam ->
                content.exam = offlineExam.asGreenDaoModel()
                return content
            }
        }
        return null
    }

    suspend fun getOfflinePausedAttempt(examId: Long): CourseAttempt? {
        getOfflineAttemptsByExamIdAndState(examId, Attempt.RUNNING).lastOrNull()
            ?.let { offlineAttempt ->
                val offlineAttemptSectionList = getOfflineAttemptSectionList(offlineAttempt.id)
                val offlineContentAttempt = getOfflineContentAttempts(offlineAttempt.id)
                return offlineContentAttempt?.createGreenDoaModel(
                    offlineAttempt.createGreenDoaModel(
                        offlineAttemptSectionList.asGreenDoaModels()
                    )
                )
            }
        return null
    }

    private fun getContentFromDB(contentId: Long): Content? {
        val contentDao = TestpressSDKDatabase.getContentDao(context)
        val contents =
            contentDao.queryBuilder().where(ContentDao.Properties.Id.eq(contentId)).list()

        if (contents.isEmpty()) {
            return null
        }
        return contents[0]
    }
}