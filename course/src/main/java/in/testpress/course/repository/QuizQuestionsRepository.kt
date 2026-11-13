package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.network.Resource
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.domain.asDomainModel
import `in`.testpress.exam.domain.asDomainModels
import `in`.testpress.exam.network.ExamNetwork
import `in`.testpress.exam.network.NetworkAnswer
import `in`.testpress.exam.network.NetworkExamQuestionResult
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.HashMap
import kotlin.random.Random

class QuizQuestionsRepository(context: Context): QuizExamRepository(context) {
    private val examNetwork = ExamNetwork(context)
    val examQuestionDao = TestpressSDKDatabase.getExamQuestionDao(context)
    private val userSelectedAnswerDao = TestpressSDKDatabase.getUserSelectedAnswerDao(context)

    var page = 1

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
        page = pageNumber
        val queryParams: HashMap<String, Any> = hashMapOf()
        queryParams["page"] = page
        examNetwork.getQuestions(url, queryParams)
            .enqueue(object: TestpressCallback<ApiResponse<NetworkExamQuestionResult>>(){
                override fun onSuccess(result: ApiResponse<NetworkExamQuestionResult>?) {
                    handleQuestionsFetchSuccess(result, examId, attemptId, url, accumulatedResults)
                }

                override fun onException(exception: TestpressException?) {
                    page = 1
                    val questions = getQuestionsFromDB(examId, attemptId)
                    if (!questions.isNullOrEmpty()) {
                        createUserSelectedAnswers(questions, attemptId)
                    } else {
                        _resourceUserSelectedAnswers.postValue(Resource.error(exception!!, null))
                    }
                }
            })
    }

    private fun handleQuestionsFetchSuccess(
        response: ApiResponse<NetworkExamQuestionResult>?,
        examId: Long,
        attemptId: Long,
        url: String,
        accumulatedResults: MutableList<NetworkExamQuestionResult>
    ) {
        response?.results?.let { accumulatedResults.add(it) }
        if (response?.next != null) {
            page += 1
            fetchQuestions(url, examId, attemptId, page, accumulatedResults)
        } else {
            page = 1
            cleanQuestionInDB(examId, attemptId)
            saveQuestionsToDB(accumulatedResults, examId, attemptId)
            val questions = getQuestionsFromDB(examId, attemptId)
            createUserSelectedAnswers(questions!!, attemptId)
        }
    }

    private fun cleanQuestionInDB(examId: Long, attemptId: Long) {
        if (attemptId != -1L) {
            examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.AttemptId.eq(attemptId))
                .buildDelete().executeDeleteWithoutDetachingEntities()
            userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.AttemptId.eq(attemptId))
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

    private fun saveQuestionsToDB(response: NetworkExamQuestionResult?, examId: Long, attemptId: Long) {
        response?.let { saveQuestionsToDB(listOf(it), examId, attemptId) }
    }

    fun getQuestions(examId: Long, attemptId: Long, url: String): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        val userSelectedAnswers = userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.AttemptId.eq(attemptId)).list()
        if (userSelectedAnswers.isNotEmpty()) {
            _resourceUserSelectedAnswers.postValue(Resource.success(userSelectedAnswers.asDomainModels()))
        } else {
            fetchQuestions(url, examId, attemptId)
        }
        return resourceUserSelectedAnswers
    }

    private fun createUserSelectedAnswers(questions: List<ExamQuestion>, attemptId: Long) {
        val userSelectedAnswerDao = TestpressSDKDatabase.getUserSelectedAnswerDao(context)
        val id = getUserSelectedAnswerID(questions.size)

        questions.forEachIndexed{index, examQuestion ->
            val question = examQuestion.question
            val correctAnswers = question.answers.filter { it.isCorrect ?: false }
            val correctAnswersIds = IntegerList()
            correctAnswersIds.addAll(correctAnswers.map {it.id.toInt()})

            val url = "/api/v2.4/attempts/${attemptId}/questions/${examQuestion.id}/"
            val userSelectedAnswer = UserSelectedAnswer(
                id + index, index, false, examQuestion.examId, attemptId,
                question.explanationHtml, null, null, null,
                correctAnswersIds, url, question.id, examQuestion.id
            )
            userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer)

        }
        val userSelectedAnswers = userSelectedAnswerDao.queryBuilder()
            .where(UserSelectedAnswerDao.Properties.AttemptId.eq(attemptId)).list()

        _resourceUserSelectedAnswers.postValue(Resource.success(userSelectedAnswers.asDomainModels()))
    }

    private fun getUserSelectedAnswerID(endIndex: Int): Long {
        val id = Random.nextLong(99999, 9999999)
        val count = userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.Id.between(id, id + endIndex)).count()
        if (count > 0) {
            getUserSelectedAnswerID(endIndex)
        }

        return id
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
        val userSelectedAnswer = getUserSelectedAnswer(id)
        userSelectedAnswer?.selectedAnswers = IntegerList()
        userSelectedAnswer?.selectedAnswers?.addAll(selectedOptions)
        userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer)
    }

    fun submitAnswer(id: Long): MutableLiveData<Resource<DomainUserSelectedAnswer>> {
        val userSelectedAnswer = getUserSelectedAnswer(id) ?: run {
            answerResource.postValue(
                Resource.error(TestpressException.unexpectedError(IllegalStateException("Answer not found")), null)
            )
            return answerResource
        }
        val answer = HashMap<String, Any>()
        answer["selected_answers"] = userSelectedAnswer.selectedAnswers ?: listOf<Integer>()
        userSelectedAnswer.shortText?.let {
            answer["short_text"] = it
        }

        val url = "/api/v2.4/attempts/${userSelectedAnswer.attemptId}/questions/${userSelectedAnswer.examQuestionId}/"
        examNetwork.saveUserSelectedAnswer(url, answer)
            .enqueue(object : TestpressCallback<NetworkUserSelectedAnswer>() {
                override fun onSuccess(result: NetworkUserSelectedAnswer?) {
                    val usa = result?.asGreenDaoModel()?.apply {
                        this.id = userSelectedAnswer.id
                        order = userSelectedAnswer.order
                        examId = userSelectedAnswer.examId
                        examQuestionId = userSelectedAnswer.examQuestionId
                        questionId = userSelectedAnswer.questionId
                        attemptId = userSelectedAnswer.attemptId
                        explanationHtml = result.explanationHtml?.takeIf { it.isNotBlank() }
                            ?: result.question?.explanationHtml?.takeIf { it.isNotBlank() }
                            ?: userSelectedAnswer.explanationHtml?.takeIf { it.isNotBlank() }
                            ?: userSelectedAnswer.question?.explanationHtml
                        correctAnswers = userSelectedAnswer.correctAnswers
                        duration = "00:00:01"
                    }
                    if (usa == null) {
                        answerResource.postValue(
                            Resource.error(TestpressException.unexpectedError(IllegalStateException("Failed to parse answer response")), null)
                        )
                        return
                    }
                    userSelectedAnswerDao.insertOrReplaceInTx(usa)
                    val updatedAnswer = getUserSelectedAnswer(usa.id)
                    answerResource.postValue(Resource.success(updatedAnswer?.asDomainModel()))
                }

                override fun onException(exception: TestpressException?) {
                    userSelectedAnswer.duration = "00:00:01"
                    userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer)
                    answerResource.postValue(Resource.error(exception!!, null))
                }
            })
        return answerResource
    }
}