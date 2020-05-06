package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.Resource
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

    private fun fetchQuestions(url: String, examId: Long, attemptId: Long) {
        val queryParams: HashMap<String, Any> = hashMapOf()
        queryParams["page"] = page
        examNetwork.getQuestions(url, queryParams)
            .enqueue(object: TestpressCallback<ApiResponse<NetworkExamQuestionResult>>(){
                override fun onSuccess(result: ApiResponse<NetworkExamQuestionResult>?) {
                    handleQuestionsFetchSuccess(result, examId, attemptId, url)
                }

                override fun onException(exception: TestpressException?) {
                    _resourceUserSelectedAnswers.postValue(Resource.error(exception!!, null))
                }
            })
    }

    private fun handleQuestionsFetchSuccess(response: ApiResponse<NetworkExamQuestionResult>?, examId: Long, attemptId: Long, url: String) {
        if (response?.next != null) {
            page += 1
            fetchQuestions(url, examId, attemptId)
            saveQuestionsToDB(response?.results, examId)
        } else {
            page = 1
            saveQuestionsToDB(response?.results, examId)
            val questions = getQuestionsFromDB(examId)
            createUserSelectedAnswers(questions!!, attemptId)
        }
    }

    private fun cleanQuestionInDB(examId: Long) {
        examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.ExamId.eq(examId))
            .buildDelete().executeDeleteWithoutDetachingEntities()
    }

    private fun getQuestionsFromDB(examId: Long): List<ExamQuestion>? {
        return examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.ExamId.eq(examId)).list()
    }

    private fun saveQuestionsToDB(response: NetworkExamQuestionResult?, examId: Long) {
        val questionDao = TestpressSDKDatabase.getQuestionDao(context)
        val answerDao = TestpressSDKDatabase.getAnswerDao(context)
        val directionDao = TestpressSDKDatabase.getDirectionDao(context)

        val questions = response?.questions
        val answers = mutableListOf<NetworkAnswer>()

        for(question in questions ?: listOf()) {
            question.answers?.map {
                it.questionId = question.id
                answers.add(it)
            }
        }
        answerDao.insertOrReplaceInTx(answers.asGreenDaoModels())
        questionDao.insertOrReplaceInTx(questions?.asGreenDaoModels())

        for(examQuestion in response?.examQuestions ?:listOf()) {
            examQuestion.examId = examId
        }

        examQuestionDao.insertOrReplaceInTx(response?.examQuestions?.asGreenDaoModels())
        directionDao.insertOrReplaceInTx(response?.directions?.asGreenDaoModels())
    }

    fun getQuestions(examId: Long, attemptId: Long, url: String): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        if(getQuestionsFromDB(examId)?.isNotEmpty() == true) {
            val userSelectedAnswers = userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.AttemptId.eq(attemptId)).list()
            if (userSelectedAnswers.isEmpty()) {
                createUserSelectedAnswers(getQuestionsFromDB(examId)!!, attemptId)
            } else {
                _resourceUserSelectedAnswers.postValue(Resource.success(userSelectedAnswers.asDomainModels()))
            }
        } else {
            cleanQuestionInDB(examId)
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
                id + index, index, false, null, attemptId,
                examQuestion.question.explanation, null, null, null,
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
        return userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.Id.eq(id)).list().get(0)
    }

    fun setAnswer(id: Long, selectedOptions: ArrayList<Int>) {
        val userSelectedAnswer = getUserSelectedAnswer(id)
        userSelectedAnswer?.selectedAnswers = IntegerList()
        userSelectedAnswer?.selectedAnswers?.addAll(selectedOptions)
        userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer)
    }

    fun submitAnswer(id: Long): MutableLiveData<Resource<DomainUserSelectedAnswer>> {
        val userSelectedAnswer = getUserSelectedAnswer(id)
        val answer = HashMap<String, Any>()
        answer["selected_answers"] = userSelectedAnswer!!.selectedAnswers ?: listOf<Integer>()
        userSelectedAnswer.shortText?.let {
            answer["short_text"] = it
        }

        val url = "/api/v2.4/attempts/${userSelectedAnswer.attemptId}/questions/${userSelectedAnswer.examQuestionId}/"
        examNetwork.saveUserSelectedAnswer(url, answer)
            .enqueue(object : TestpressCallback<NetworkUserSelectedAnswer>() {
                override fun onSuccess(result: NetworkUserSelectedAnswer?) {
                    var usa = result?.asGreenDaoModel()
                    usa?.questionId = userSelectedAnswer.questionId
                    usa?.attemptId = userSelectedAnswer.attemptId
                    usa?.duration = "00:00:01"
                    userSelectedAnswerDao.insertOrReplaceInTx(usa)
                    val userSelectedAnswer = getUserSelectedAnswer(usa?.id!!)
                    answerResource.postValue(Resource.success(userSelectedAnswer?.asDomainModel()))
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