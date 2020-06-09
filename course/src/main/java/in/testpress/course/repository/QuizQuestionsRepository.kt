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
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.HashMap

class QuizQuestionsRepository(context: Context): QuizExamRepository(context) {
    private val examNetwork = ExamNetwork(context)
    val examQuestionDao = TestpressSDKDatabase.getExamQuestionDao(context)
    private val userSelectedAnswerDao = TestpressSDKDatabase.getUserSelectedAnswerDao(context)
    var page = 1
    var _resourceUserSelectedAnswers: MutableLiveData<Resource<List<DomainUserSelectedAnswer>>> = MutableLiveData()
    val resourceUserSelectedAnswers: LiveData<Resource<List<DomainUserSelectedAnswer>>>
        get() = _resourceUserSelectedAnswers
    val answerResource: MutableLiveData<Resource<DomainUserSelectedAnswer>> = MutableLiveData()
    private val userSelectedAnswersHandler = UserSelectedAnswersHandler(context)


    private fun fetchQuestions(examId: Long, url: String, onSuccess:(questions: List<ExamQuestion>) -> Unit) {
        val queryParams: HashMap<String, Any> = hashMapOf()
        queryParams["page"] = page

        examNetwork.getQuestions(url, queryParams)
            .enqueue(object: TestpressCallback<ApiResponse<NetworkExamQuestionResult>>(){
                override fun onSuccess(response: ApiResponse<NetworkExamQuestionResult>?) {
                    storeExamQuestions(response?.results, examId)
                    if (response!!.hasNextPage()) {
                        page += 1
                        fetchQuestions(examId, url, onSuccess)
                    } else {
                        page = 1
                        getQuestionsFromDB(examId)?.let { onSuccess(it) }
                    }
                }

                override fun onException(exception: TestpressException?) {
                    _resourceUserSelectedAnswers.postValue(Resource.error(exception!!, null))
                }
            })
    }

    private fun cleanQuestionInDB(examId: Long) {
        examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.ExamId.eq(examId))
            .buildDelete().executeDeleteWithoutDetachingEntities()
    }

    private fun getQuestionsFromDB(examId: Long): List<ExamQuestion>? {
        return examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.ExamId.eq(examId)).list()
    }

    private fun storeExamQuestions(response: NetworkExamQuestionResult?, examId: Long) {
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
            var userSelectedAnswers = userSelectedAnswersHandler.getForAttempt(attemptId)
            if (userSelectedAnswers.isNullOrEmpty()) {
                userSelectedAnswersHandler.create(attemptId, getQuestionsFromDB(examId)!!)
                userSelectedAnswers = userSelectedAnswersHandler.getForAttempt(attemptId)
            }
            _resourceUserSelectedAnswers.postValue(Resource.success(userSelectedAnswers?.asDomainModels()))
        } else {
            cleanQuestionInDB(examId)
            fetchQuestions(examId, url) {
                userSelectedAnswersHandler.create(attemptId, it)
                getUserSelectedAnswers(attemptId)
            }
        }
        return resourceUserSelectedAnswers
    }

    fun getUserSelectedAnswers(attemptId: Long): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        userSelectedAnswersHandler.getForAttempt(attemptId)?.let {
            _resourceUserSelectedAnswers.postValue(Resource.success(it.asDomainModels()))
        }
        return resourceUserSelectedAnswers
    }

    fun setAnswer(id: Long, selectedOptions: ArrayList<Int>) {
        userSelectedAnswersHandler.setSelectedOptions(id, selectedOptions)
    }

    fun submitAnswer(id: Long): MutableLiveData<Resource<DomainUserSelectedAnswer>> {
        val data = userSelectedAnswersHandler.getDataForSubmission(id)
        val userSelectedAnswer = userSelectedAnswersHandler.get(id)
        val url = "/api/v2.4/attempts/${userSelectedAnswer?.attemptId}/questions/${userSelectedAnswer?.examQuestionId}/"

        examNetwork.saveUserSelectedAnswer(url, data)
            .enqueue(object : TestpressCallback<NetworkUserSelectedAnswer>() {
                override fun onSuccess(result: NetworkUserSelectedAnswer?) {
                    userSelectedAnswersHandler.restore(id, result?.asGreenDaoModel())
                    val userSelectedAnswer = userSelectedAnswersHandler.get(result?.id!!)
                    answerResource.postValue(Resource.success(userSelectedAnswer?.asDomainModel()))
                }

                override fun onException(exception: TestpressException?) {
                    userSelectedAnswer?.duration = "00:00:01"
                    userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer)
                    answerResource.postValue(Resource.error(exception!!, null))
                }
            })
        return answerResource
    }
}