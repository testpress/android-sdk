package `in`.testpress.exam.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttemptItem
import `in`.testpress.database.entities.OfflineCourseAttempt
import `in`.testpress.database.mapping.asGreenDoaModels
import `in`.testpress.database.mapping.createGreenDoaModel
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.models.AttemptItem
import `in`.testpress.exam.network.NetworkAttemptSection
import `in`.testpress.exam.network.asNetworkModel
import `in`.testpress.exam.ui.TestFragment.Action
import `in`.testpress.exam.util.asAttemptItem
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.Exam
import `in`.testpress.network.Resource
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AttemptRepository(val context: Context) {

    lateinit var exam : Exam
    lateinit var attempt : Attempt
    private val isOfflineExam: Boolean get() = exam.isOfflineExam ?: false
    var page = 1
    val attemptItem = mutableListOf<AttemptItem>()
    private var _totalQuestions = 0
    val totalQuestions get() = _totalQuestions

    private val database = TestpressDatabase.invoke(context)
    private val examQuestionDao = database.examQuestionDao()
    private val questionDao = database.questionDao()
    private val offlineAttemptItemDao = database.offlineAttemptItemDoa()
    private val subjectDao = database.subjectDao()
    private val directionDao = database.directionDao()
    private val offlineAttemptSectionDao = database.offlineAttemptSectionDao()
    private val offlineAttemptDao = database.offlineAttemptDao()
    private val offlineCourseAttemptDao = database.offlineCourseAttemptDao()
    private val offlineExamDao = database.offlineExamDao()


    private val apiClient: TestpressExamApiClient = TestpressExamApiClient(context)
    private val _attemptItemsResource = MutableLiveData<Resource<List<AttemptItem>>>()
    val attemptItemsResource: LiveData<Resource<List<AttemptItem>>> get() = _attemptItemsResource

    private val _saveResultResource = MutableLiveData<Resource<Triple<Int, AttemptItem?, Action>>>()
    val saveResultResource: LiveData<Resource<Triple<Int, AttemptItem?, Action>>> get() = _saveResultResource

    private val _updateSectionResource = MutableLiveData<Resource<Pair<NetworkAttemptSection?,Action>>>()
    val updateSectionResource: LiveData<Resource<Pair<NetworkAttemptSection?,Action>>> get() = _updateSectionResource

    private val _endContentAttemptResource = MutableLiveData<Resource<CourseAttempt>>()
    val endContentAttemptResource: LiveData<Resource<CourseAttempt>> get() = _endContentAttemptResource

    private val _endAttemptResource = MutableLiveData<Resource<Attempt>>()
    val endAttemptResource: LiveData<Resource<Attempt>> get() = _endAttemptResource

    fun fetchAttemptItems(questionsUrlFrag: String, fetchSinglePageOnly: Boolean) {
        _attemptItemsResource.postValue(Resource.loading(null))

        if (isOfflineExam){
            createOfflineAttemptItemItem()
        } else {
            val queryParams = hashMapOf<String, Any>("page" to page)
            apiClient.getQuestions(questionsUrlFrag, queryParams)
                .enqueue(object : TestpressCallback<TestpressApiResponse<AttemptItem>>() {
                    override fun onSuccess(result: TestpressApiResponse<AttemptItem>) {
                        if (fetchSinglePageOnly) {
                            _totalQuestions = result.count
                            attemptItem.addAll(result.results)
                            _attemptItemsResource.postValue(Resource.success(attemptItem))
                            if (result.hasMore()) {
                                page++
                            }
                            return
                        }
                        if (result.hasMore()) {
                            _totalQuestions = result.count
                            attemptItem.addAll(result.results)
                            page++
                            fetchAttemptItems(questionsUrlFrag, fetchSinglePageOnly)
                        } else {
                            attemptItem.addAll(result.results)
                            _attemptItemsResource.postValue(Resource.success(attemptItem))
                        }
                    }

                    override fun onException(exception: TestpressException) {
                        _attemptItemsResource.postValue(Resource.error(exception, null))
                    }

                })
        }
    }

    private fun createOfflineAttemptItemItem() {
        CoroutineScope(Dispatchers.IO).launch {
            if (isAttemptItemAlreadyCreated()){
                if (attempt.hasSectionalLock()){
                    if (isAttemptItemAlreadyCreatedForSection()){
                        createAttemptItems()
                    } else {
                        createOfflineAttemptItemsForSections(attempt.sections[attempt.currentSectionPosition].attemptSectionId)
                    }
                } else {
                    createAttemptItems()
                }
            } else {
                if (attempt.hasSectionalLock()){
                    createOfflineAttemptItemsForSections(attempt.sections[attempt.currentSectionPosition].attemptSectionId)
                } else {
                    createOfflineAttemptItemsForAllQuestions()
                }
            }
        }
    }

    private suspend fun isAttemptItemAlreadyCreated():Boolean{
        return offlineAttemptItemDao.getOfflineAttemptItemCountByAttemptId(attempt.id) != 0
    }

    private suspend fun isAttemptItemAlreadyCreatedForSection():Boolean{
        var offlineAttemptItems = offlineAttemptItemDao.getOfflineAttemptItemByAttemptId(attempt.id)
        if (attempt.hasSectionalLock()){
            offlineAttemptItems = offlineAttemptItems.filter { it.attemptSection!!.id.toInt() == attempt.currentSectionPosition  }
        }
        return offlineAttemptItems.isNotEmpty()
    }

    private suspend fun createOfflineAttemptItemsForSections(attemptSectionId: Long) {
        val offlineAttemptSection = offlineAttemptSectionDao.getByAttemptSectionId(attemptSectionId)
        val examQuestions = examQuestionDao.getExamQuestionsByExamIdAndSectionId(exam.id, offlineAttemptSection?.sectionId!!)
        val offlineAttemptItems = examQuestions.map { examQuestion ->
            OfflineAttemptItem(
                question = questionDao.getQuestionById(examQuestion.questionId!!)!!,
                order = examQuestion.order!!,
                attemptSection = offlineAttemptSectionDao.getBySectionId(examQuestion.sectionId),
                attemptId = attempt.id
            )
        }
        offlineAttemptItemDao.insertAll(offlineAttemptItems)
        createAttemptItems()
    }

    private suspend fun createOfflineAttemptItemsForAllQuestions() {
        val examQuestions = examQuestionDao.getExamQuestionsByExamId(exam.id)
        val offlineAttemptItems = examQuestions.map { examQuestion ->
            OfflineAttemptItem(
                question = questionDao.getQuestionById(examQuestion.questionId!!)!!,
                order = examQuestion.order!!,
                attemptSection = offlineAttemptSectionDao.getBySectionId(examQuestion.sectionId),
                attemptId = attempt.id
            )
        }
        offlineAttemptItemDao.insertAll(offlineAttemptItems)
        createAttemptItems()
    }

    private suspend fun createAttemptItems(){
        var offlineAttemptItems = offlineAttemptItemDao.getOfflineAttemptItemByAttemptId(attempt.id)
        if (attempt.hasSectionalLock()){
            offlineAttemptItems = offlineAttemptItems.filter { it.attemptSection!!.id.toInt() == attempt.currentSectionPosition  }
        }
        val attemptItems = offlineAttemptItems.map { offlineAttemptItem ->
            val subject = offlineAttemptItem.question.subjectId?.let { subjectDao.getSubjectById(it) }
            val direction = offlineAttemptItem.question.directionId?.let { directionDao.getDirectionById(it) }
            val subjectName = subject?.name ?: "Uncategorized"
            val directionHtml = direction?.html
            offlineAttemptItem.asAttemptItem(
                subjectName,
                directionHtml,
            )
        }
        _attemptItemsResource.postValue(Resource.success(attemptItems))
    }

    suspend fun saveAnswer(position: Int, attemptItem: AttemptItem, action: Action, remainingTime: String) {
        if (isOfflineExam){
            if (attempt.hasSectionalLock()){
                offlineAttemptSectionDao.getRemainingTimeByAttemptSectionId(
                    attempt.sections[attempt.currentSectionPosition].attemptSectionId,
                    remainingTime
                )
            } else {
                offlineAttemptDao.updateRemainingTimeAndLastStartedTime(attempt.id, remainingTime, Date().toString())
            }
            if (action == Action.PAUSE){
                offlineExamDao.updatePausedAttemptCount(exam.id, 1L)
            }
            updateLocalAttemptItem(attemptItem) { updateAttemptItem ->
                _saveResultResource.postValue(Resource.success(Triple(position, updateAttemptItem, action)))
            }
        } else {
            apiClient.postAnswer(attemptItem).enqueue(object : TestpressCallback<AttemptItem>() {
                override fun onSuccess(result: AttemptItem) {
                    _saveResultResource.postValue(Resource.success(Triple(position, result, action)))
                }

                override fun onException(exception: TestpressException) {
                    _saveResultResource.postValue(
                        Resource.error(
                            exception,
                            Triple(position, null, action)
                        )
                    )
                }
            })
        }
    }

    private suspend fun updateLocalAttemptItem(
        attemptItem: AttemptItem,
        callback: (AttemptItem) -> Unit
    ) {
        val offlineAttemptItem =
            offlineAttemptItemDao.getAttemptItemById(attemptItem.id.toLong())
        if (offlineAttemptItem != null) {
            if (attemptItem.attemptQuestion.type == "E") {
                offlineAttemptItem.localEssayText = attemptItem.localEssayText
            } else {
                offlineAttemptItem.selectedAnswers = attemptItem.savedAnswers
                offlineAttemptItem.savedAnswers = attemptItem.savedAnswers
                offlineAttemptItem.currentShortText = attemptItem.currentShortText
            }
            offlineAttemptItem.unSyncedFiles = attemptItem.unSyncedFiles
            offlineAttemptItem.review = attemptItem.currentReview
            offlineAttemptItemDao.update(offlineAttemptItem)
        }
        val updatedAttemptItem = offlineAttemptItem!!.asAttemptItem(
            attemptItem.attemptQuestion.subject,
            attemptItem.attemptQuestion.direction
        )
        callback.invoke(updatedAttemptItem)
    }

    fun updateSection(url: String, action: Action) {
        _updateSectionResource.postValue(Resource.loading(Pair(null, action)))
        if (isOfflineExam){
            CoroutineScope(Dispatchers.IO).launch {
                updateOfflineSection(action)
            }
        } else {
            apiClient.updateSection(url).enqueue(object : TestpressCallback<NetworkAttemptSection>() {
                override fun onSuccess(result: NetworkAttemptSection) {
                    _updateSectionResource.postValue(Resource.success(Pair(result, action)))
                }

                override fun onException(exception: TestpressException) {
                    _updateSectionResource.postValue(Resource.error(exception, Pair(null, action)))
                }
            })
        }
    }

    private suspend fun updateOfflineSection(action: Action) {
        when (action){
            Action.END_SECTION -> {
                val offlineAttemptSection = offlineAttemptSectionDao.getByAttemptIdAndId(attempt.id, attempt.currentSectionPosition.toLong())
                offlineAttemptSection?.state = Attempt.COMPLETED
                offlineAttemptSectionDao.update(offlineAttemptSection!!)
                attemptItem.clear()
                _updateSectionResource.postValue(Resource.success(Pair(offlineAttemptSection.asNetworkModel(), action)))
            }
            Action.START_SECTION -> {
                val offlineAttemptSection = offlineAttemptSectionDao.getByAttemptIdAndId(attempt.id, attempt.currentSectionPosition.toLong())
                offlineAttemptSection?.state = Attempt.RUNNING
                offlineAttemptSectionDao.update(offlineAttemptSection!!)
                _updateSectionResource.postValue(Resource.success(Pair(offlineAttemptSection.asNetworkModel(), action)))
            }
            else -> {}
        }

    }

    fun endContentAttempt(attemptEndFrag: String) {
        _endContentAttemptResource.postValue(Resource.loading(null))
        if (isOfflineExam) {
            CoroutineScope(Dispatchers.IO).launch {
                endAllOfflineAttemptSection()
                offlineAttemptDao.updateAttemptState(attempt.id,Attempt.COMPLETED)
                offlineExamDao.updatePausedAttemptCount(exam.id, 0L)
                val offlineCourseAttempt = offlineCourseAttemptDao.getById(attempt.id)
                _endContentAttemptResource.postValue(Resource.success(offlineCourseAttempt!!.createGreenDoaModel(attempt)))
            }
        } else {
            apiClient.endContentAttempt(attemptEndFrag)
                .enqueue(object : TestpressCallback<CourseAttempt>() {
                    override fun onSuccess(result: CourseAttempt) {
                        _endContentAttemptResource.postValue(Resource.success(result))
                    }

                    override fun onException(exception: TestpressException) {
                        _endContentAttemptResource.postValue(Resource.error(exception, null))
                    }
                })
        }
    }

    fun endAttempt(attemptEndFrag: String) {
        _endAttemptResource.postValue(Resource.loading(null))
        if (isOfflineExam) {
            CoroutineScope(Dispatchers.IO).launch {
                endAllOfflineAttemptSection()
                offlineAttemptDao.updateAttemptState(attempt.id,Attempt.COMPLETED)
                offlineExamDao.updatePausedAttemptCount(exam.id, 0L)
                val offlineAttempt = offlineAttemptDao.getById(attempt.id)
                val offlineAttemptSections = offlineAttemptSectionDao.getByAttemptId(attempt.id)
                _endAttemptResource.postValue(Resource.success(offlineAttempt.createGreenDoaModel(offlineAttemptSections.asGreenDoaModels())))
            }
        } else {
            apiClient.endAttempt(attemptEndFrag)
                .enqueue(object : TestpressCallback<Attempt>() {
                    override fun onSuccess(response: Attempt) {
                        _endAttemptResource.postValue(Resource.success(response))
                    }

                    override fun onException(exception: TestpressException) {
                        _endAttemptResource.postValue(Resource.error(exception, null))
                    }
                })
        }
    }

    private suspend fun endAllOfflineAttemptSection() {
        val runningOfflineAttemptSection =
            offlineAttemptSectionDao.getOfflineAttemptSectionsByAttemptIdAndStates(
                attempt.id,
                listOf(Attempt.NOT_STARTED, Attempt.RUNNING)
            )
        runningOfflineAttemptSection.forEach {
            it.state = Attempt.COMPLETED
            offlineAttemptSectionDao.update(it)
        }
    }

    fun clearAttemptItem() {
        attemptItem.clear()
    }

    fun resetPageCount() {
        page = 1
    }

}