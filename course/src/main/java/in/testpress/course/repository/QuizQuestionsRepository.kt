package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.network.Resource
import `in`.testpress.exam.domain.DomainQuestion
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.domain.asDomainModel
import `in`.testpress.exam.domain.asDomainModels
import `in`.testpress.exam.domain.getResolvedExplanation
import `in`.testpress.exam.network.ExamNetwork
import `in`.testpress.exam.network.NetworkAnswer
import `in`.testpress.exam.network.NetworkDirection
import `in`.testpress.exam.network.NetworkExamQuestion
import `in`.testpress.exam.network.NetworkExamQuestionResult
import `in`.testpress.exam.network.NetworkQuestion
import `in`.testpress.exam.network.NetworkUserSelectedAnswer
import `in`.testpress.exam.network.asGreenDaoModel
import `in`.testpress.exam.network.asGreenDaoModels
import `in`.testpress.models.greendao.ExamQuestion
import `in`.testpress.models.greendao.ExamQuestionDao
import `in`.testpress.models.greendao.UserSelectedAnswer
import `in`.testpress.models.greendao.UserSelectedAnswerDao
import `in`.testpress.util.IntegerList
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.Collections
import java.util.HashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.random.Random

class QuizQuestionsRepository(context: Context): QuizExamRepository(context) {
    private val examNetwork = ExamNetwork(context)
    val examQuestionDao = TestpressSDKDatabase.getExamQuestionDao(context)
    private val userSelectedAnswerDao = TestpressSDKDatabase.getUserSelectedAnswerDao(context)

    companion object {
        private const val TAG = "QuizQuestionsRepo"
        private val dbExecutor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "QuizQuestionsDbThread").apply {
                isDaemon = true
            }
        }
    }

    private val inFlightAttemptIds = Collections.synchronizedSet(mutableSetOf<Long>())

    var _resourceUserSelectedAnswers: MutableLiveData<Resource<List<DomainUserSelectedAnswer>>> = MutableLiveData()
    val resourceUserSelectedAnswers: LiveData<Resource<List<DomainUserSelectedAnswer>>>
        get() = _resourceUserSelectedAnswers
    val answerResource: MutableLiveData<Resource<DomainUserSelectedAnswer>> = MutableLiveData()

    private fun fetchQuestions(
        url: String,
        examId: Long,
        attemptId: Long,
        pageNumber: Int = 1,
        accumulatedResults: MutableList<NetworkExamQuestionResult> = mutableListOf()
    ) {
        val queryParams: HashMap<String, Any> = hashMapOf()
        queryParams["page"] = pageNumber
        examNetwork.getQuestions(url, queryParams)
            .enqueue(object: TestpressCallback<ApiResponse<NetworkExamQuestionResult>>(){
                override fun onSuccess(result: ApiResponse<NetworkExamQuestionResult>?) {
                    handleQuestionsFetchSuccess(result, examId, attemptId, url, pageNumber, accumulatedResults)
                }

                override fun onException(exception: TestpressException?) {
                    inFlightAttemptIds.remove(attemptId)
                    CoroutineScope(Dispatchers.IO).launch {
                        loadQuestionsFromDBAndPost(
                            examId,
                            attemptId,
                            exception ?: TestpressException.unexpectedError(IllegalStateException("Unknown error"))
                        )
                    }
                }
            })
    }

    private fun handleQuestionsFetchSuccess(
        response: ApiResponse<NetworkExamQuestionResult>?,
        examId: Long,
        attemptId: Long,
        url: String,
        pageNumber: Int,
        accumulatedResults: MutableList<NetworkExamQuestionResult>
    ) {
        response?.results?.let { accumulatedResults.add(it) }
        if (response?.next != null) {
            fetchQuestions(url, examId, attemptId, pageNumber + 1, accumulatedResults)
        } else {
            inFlightAttemptIds.remove(attemptId)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (accumulatedResults.isNotEmpty()) {
                        TestpressSDKDatabase.getDaoSession(context).runInTx {
                            cleanQuestionInDB(examId, attemptId)
                            saveQuestionsToDB(accumulatedResults, examId, attemptId)
                        }
                    }
                    loadQuestionsFromDBAndPost(
                        examId,
                        attemptId,
                        TestpressException.unexpectedError(IllegalStateException("No questions found"))
                    )
                } catch (e: Exception) {
                    _resourceUserSelectedAnswers.postValue(
                        Resource.error(TestpressException.unexpectedError(e), null)
                    )
                }
            }
        }
    }

    private fun loadQuestionsFromDBAndPost(examId: Long, attemptId: Long, fallbackException: TestpressException) {
        try {
            val questions = getQuestionsFromDB(examId, attemptId)
            if (!questions.isNullOrEmpty()) {
                createUserSelectedAnswers(questions, attemptId)
            } else {
                _resourceUserSelectedAnswers.postValue(
                    Resource.error(fallbackException, null)
                )
            }
        } catch (e: Exception) {
            _resourceUserSelectedAnswers.postValue(
                Resource.error(TestpressException.unexpectedError(e), null)
            )
        }
    }

    private fun cleanQuestionInDB(examId: Long, attemptId: Long) {
        if (attemptId != -1L) {
            examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.AttemptId.eq(attemptId))
                .buildDelete().executeDeleteWithoutDetachingEntities()
        } else if (examId != -1L) {
            examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.ExamId.eq(examId))
                .buildDelete().executeDeleteWithoutDetachingEntities()
        }
    }

    private fun getQuestionsFromDB(examId: Long, attemptId: Long): List<ExamQuestion>? {
        if (attemptId != -1L) {
            val questions = examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.AttemptId.eq(attemptId)).list()
            if (!questions.isNullOrEmpty()) {
                return questions
            }
        }
        // Fall back to all questions for the examId if questions for attemptId are not found.
        // This is intentional offline fallback behavior, allowing cached questions from a previous attempt of the exam to be used.
        // Note: Exam-question IDs are exam-scoped, remaining stable across attempts.
        return if (examId != -1L) {
            examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.ExamId.eq(examId)).list()
        } else {
            null
        }
    }

    private fun saveQuestionsToDB(results: List<NetworkExamQuestionResult>, examId: Long, attemptId: Long) {
        val questionDao = TestpressSDKDatabase.getQuestionDao(context)
        val answerDao = TestpressSDKDatabase.getAnswerDao(context)
        val directionDao = TestpressSDKDatabase.getDirectionDao(context)

        val answers = mutableListOf<NetworkAnswer>()
        val questions = mutableListOf<NetworkQuestion>()
        val examQuestions = mutableListOf<NetworkExamQuestion>()
        val directions = mutableListOf<NetworkDirection>()

        val defaultExamId = if (examId == -1L) null else examId
        for (result in results) {
            result.questions?.let { questions.addAll(it) }
            for (question in result.questions ?: listOf()) {
                question.answers?.map {
                    it.questionId = question.id
                    answers.add(it)
                }
            }

            for (examQuestion in result.examQuestions ?: listOf()) {
                examQuestion.examId = defaultExamId
                examQuestion.attemptId = attemptId
                examQuestions.add(examQuestion)
            }

            result.directions?.let { directions.addAll(it) }
        }

        answerDao.insertOrReplaceInTx(answers.asGreenDaoModels())
        questionDao.insertOrReplaceInTx(questions.asGreenDaoModels())
        examQuestionDao.insertOrReplaceInTx(examQuestions.asGreenDaoModels())
        directionDao.insertOrReplaceInTx(directions.asGreenDaoModels())
    }

    fun getQuestions(examId: Long, attemptId: Long, url: String): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        val userSelectedAnswers = userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.AttemptId.eq(attemptId)).list()
        if (userSelectedAnswers.isNotEmpty()) {
            _resourceUserSelectedAnswers.postValue(Resource.success(userSelectedAnswers.asDomainModels()))
        } else if (!inFlightAttemptIds.contains(attemptId)) {
            inFlightAttemptIds.add(attemptId)
            try {
                fetchQuestions(url, examId, attemptId)
            } catch (e: Exception) {
                inFlightAttemptIds.remove(attemptId)
                _resourceUserSelectedAnswers.postValue(
                    Resource.error(TestpressException.unexpectedError(e), null)
                )
            }
        }
        return resourceUserSelectedAnswers
    }

    private fun createUserSelectedAnswers(questions: List<ExamQuestion>, attemptId: Long) {
        val userSelectedAnswerDao = TestpressSDKDatabase.getUserSelectedAnswerDao(context)
        val id = getUserSelectedAnswerID(questions.size)

        val userSelectedAnswersList = questions.mapIndexed { index, examQuestion ->
            val question = examQuestion.question
            val correctAnswers = question.answers.filter { it.isCorrect ?: false }
            val correctAnswersIds = IntegerList()
            correctAnswersIds.addAll(correctAnswers.map { it.id.toInt() })

            val url = "/api/v2.4/attempts/${attemptId}/questions/${examQuestion.id}/"
            UserSelectedAnswer(
                id + index, index, false, examQuestion.examId, attemptId,
                question.explanationHtml, null, null, null,
                correctAnswersIds, url, question.id, examQuestion.id
            )
        }
        TestpressSDKDatabase.getDaoSession(context).runInTx {
            userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswersList)
        }
        val userSelectedAnswers = userSelectedAnswerDao.queryBuilder()
            .where(UserSelectedAnswerDao.Properties.AttemptId.eq(attemptId)).list()

        _resourceUserSelectedAnswers.postValue(Resource.success(userSelectedAnswers.asDomainModels()))
    }

    private fun getUserSelectedAnswerID(count: Int): Long {
        while (true) {
            val id = Random.nextLong(99999, 9999999)
            val used = userSelectedAnswerDao.queryBuilder()
                .where(UserSelectedAnswerDao.Properties.Id.between(id, id + count - 1)).count()
            if (used == 0L) return id
        }
    }

    fun getUserSelectedAnswersFromDB(attemptId: Long): List<UserSelectedAnswer>? {
        return userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.AttemptId.eq(attemptId)).list()
    }

    fun getUserSelectedAnswers(attemptId: Long): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        val userSelectedAnswers = getUserSelectedAnswersFromDB(attemptId)
        if (userSelectedAnswers?.isNotEmpty() == true) {
            _resourceUserSelectedAnswers.postValue(Resource.success(userSelectedAnswers?.asDomainModels()))
        }
        return resourceUserSelectedAnswers
    }

    private fun getUserSelectedAnswer(id: Long): UserSelectedAnswer? {
        val answers = userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.Id.eq(id)).list()
        return answers.firstOrNull()
    }

    fun setAnswer(id: Long, selectedOptions: ArrayList<Int>) {
        dbExecutor.execute {
            try {
                val answer = getUserSelectedAnswer(id) ?: return@execute
                answer.selectedAnswers = IntegerList().apply { addAll(selectedOptions) }
                userSelectedAnswerDao.insertOrReplaceInTx(answer)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save selected options", e)
            }
        }
    }

    fun setShortText(id: Long, shortText: String) {
        dbExecutor.execute {
            try {
                val answer = getUserSelectedAnswer(id) ?: return@execute
                val trimmed = shortText.trim().ifEmpty { null }
                if (answer.shortText != trimmed) {
                    answer.shortText = trimmed
                    answer.selectedAnswers = IntegerList()
                    userSelectedAnswerDao.insertOrReplaceInTx(answer)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save short text", e)
            }
        }
    }

    fun submitAnswer(id: Long): MutableLiveData<Resource<DomainUserSelectedAnswer>> {
        dbExecutor.execute {
            try {
                val userSelectedAnswer = getUserSelectedAnswer(id) ?: run {
                    postAnswerNotFoundError()
                    return@execute
                }
                val answer = buildAnswerRequest(userSelectedAnswer)
                val url = buildAnswerUrl(userSelectedAnswer.attemptId, userSelectedAnswer.examQuestionId)

                examNetwork.saveUserSelectedAnswer(url, answer)
                    .enqueue(object : TestpressCallback<NetworkUserSelectedAnswer>() {
                        override fun onSuccess(result: NetworkUserSelectedAnswer?) {
                            handleAnswerSuccess(result, userSelectedAnswer)
                        }

                        override fun onException(exception: TestpressException?) {
                            handleAnswerFailure(exception, userSelectedAnswer)
                        }
                    })
            } catch (e: Exception) {
                answerResource.postValue(Resource.error(TestpressException.unexpectedError(e), null))
            }
        }
        return answerResource
    }

    private fun buildAnswerRequest(userSelectedAnswer: UserSelectedAnswer): HashMap<String, Any> {
        val answer = HashMap<String, Any>()
        answer["selected_answers"] = userSelectedAnswer.selectedAnswers ?: listOf<Integer>()
        userSelectedAnswer.shortText?.let {
            answer["short_text"] = it
        }
        return answer
    }

    private fun buildAnswerUrl(attemptId: Long?, examQuestionId: Long?): String {
        return "/api/v2.4/attempts/$attemptId/questions/$examQuestionId/"
    }

    private fun handleAnswerSuccess(
        result: NetworkUserSelectedAnswer?,
        userSelectedAnswer: UserSelectedAnswer
    ) {
        val usa = result?.asGreenDaoModel()?.apply {
            this.id = userSelectedAnswer.id
            order = userSelectedAnswer.order
            examId = userSelectedAnswer.examId
            examQuestionId = userSelectedAnswer.examQuestionId
            questionId = userSelectedAnswer.questionId
            attemptId = userSelectedAnswer.attemptId
            // Local row is replaced by the server response; edits are expected to be blocked once submit starts.
            shortText = result.shortText ?: userSelectedAnswer.shortText
            val freshExplanation = DomainUserSelectedAnswer(
                explanationHtml = result.explanationHtml,
                question = result.question?.let { DomainQuestion(explanation = it.explanationHtml) }
            ).getResolvedExplanation()
            val cachedExplanation = userSelectedAnswer.asDomainModel().getResolvedExplanation()
            explanationHtml = freshExplanation ?: cachedExplanation
            correctAnswers = userSelectedAnswer.correctAnswers
            duration = "00:00:01"
        }
        if (usa == null) {
            answerResource.postValue(
                Resource.error(
                    TestpressException.unexpectedError(IllegalStateException("Received empty answer response")),
                    null
                )
            )
            return
        }
        dbExecutor.execute {
            try {
                userSelectedAnswerDao.insertOrReplaceInTx(usa)
                val updatedAnswer = getUserSelectedAnswer(usa.id)
                answerResource.postValue(Resource.success(updatedAnswer?.asDomainModel()))
            } catch (e: Exception) {
                answerResource.postValue(Resource.error(TestpressException.unexpectedError(e), null))
            }
        }
    }

    private fun handleAnswerFailure(
        exception: TestpressException?,
        userSelectedAnswer: UserSelectedAnswer
    ) {
        dbExecutor.execute {
            try {
                // userSelectedAnswer is a stale snapshot taken before the network call.
                // Re-read by id here and update only duration, otherwise a failed submit overwrites newer shortText/selectedAnswers.
                getUserSelectedAnswer(userSelectedAnswer.id)?.let {
                    it.duration = "00:00:01"
                    userSelectedAnswerDao.insertOrReplaceInTx(it)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist duration", e)
            } finally {
                val error = exception ?: TestpressException.unexpectedError(IllegalStateException("Unknown error"))
                answerResource.postValue(Resource.error(error, null))
            }
        }
    }

    private fun postAnswerNotFoundError() {
        answerResource.postValue(
            Resource.error(
                TestpressException.unexpectedError(IllegalStateException("Answer not found")),
                null
            )
        )
    }
}