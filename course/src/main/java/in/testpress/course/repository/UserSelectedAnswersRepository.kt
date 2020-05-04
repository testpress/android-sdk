package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.Resource
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.domain.asDomainModels
import `in`.testpress.exam.network.ExamNetwork
import `in`.testpress.exam.network.NetworkAnswer
import `in`.testpress.exam.network.NetworkUserSelectedAnswer
import `in`.testpress.exam.network.asGreenDaoModel
import `in`.testpress.exam.network.asGreenDaoModels
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.UserSelectedAnswer
import `in`.testpress.models.greendao.UserSelectedAnswerDao
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.HashMap

class UserSelectedAnswersRepository(context: Context): QuizExamRepository(context) {
    private val examNetwork = ExamNetwork(context)
    val userSelectedAnswerDao = TestpressSDKDatabase.getUserSelectedAnswerDao(context)

    var _resourceUserSelectedAnswers: MutableLiveData<Resource<List<DomainUserSelectedAnswer>>> = MutableLiveData()
    val resourceUserSelectedAnswers: LiveData<Resource<List<DomainUserSelectedAnswer>>>
        get() = _resourceUserSelectedAnswers
    val answerResource: MutableLiveData<Resource<NetworkUserSelectedAnswer>> = MutableLiveData()

    private fun fetchUserSelectedAnswers(url: String, examId: Long) {
        examNetwork.getUserSelectedAnswers(url)
            .enqueue(object: TestpressCallback<TestpressApiResponse<NetworkUserSelectedAnswer>>() {
                override fun onSuccess(result: TestpressApiResponse<NetworkUserSelectedAnswer>?) {
                    handleUserSelectedAnswersFetchSuccess(result!!, examId)
                }

                override fun onException(exception: TestpressException?) {
                    _resourceUserSelectedAnswers.postValue(Resource.error(exception!!, null))
                }
            })
    }

    private fun handleUserSelectedAnswersFetchSuccess(response: TestpressApiResponse<NetworkUserSelectedAnswer>, examId: Long) {
        if(response.next != null) {
            fetchUserSelectedAnswers(response.next, examId)
        } else {
            saveAttemptItemsToDB(response.results, examId)
            getUserSelectedAnswers(examId)
        }
    }

    fun saveAttemptItemsToDB(userSelectedAnswers: List<NetworkUserSelectedAnswer>, examId: Long) {
        val questionDao = TestpressSDKDatabase.getQuestionDao(context)
        val answerDao = TestpressSDKDatabase.getAnswerDao(context)
        val answers = mutableListOf<NetworkAnswer>()

        for (userSelectedAnswer in userSelectedAnswers) {
            // TODO: Remove the below assigning of question id after exposing id in API
            userSelectedAnswer.question?.id = (3000..9999).random().toLong()
            userSelectedAnswer.examId = examId
            userSelectedAnswer.questionId = userSelectedAnswer.question?.id
            questionDao.insertOrReplaceInTx(userSelectedAnswer.question?.asGreenDaoModel())
            userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer.asGreenDaoModel())

            userSelectedAnswer.question?.answers?.map {
                it.questionId = userSelectedAnswer.question!!.id
                answers.add(it)
            }
            answerDao.insertOrReplaceInTx(answers.asGreenDaoModels())
        }
    }

    fun clearUserSelectedAnswersFromDB(examId: Long) {
        userSelectedAnswerDao.queryBuilder()
            .where(UserSelectedAnswerDao.Properties.ExamId.eq(examId))
            .buildDelete().executeDeleteWithoutDetachingEntities()
    }

    fun getUserSelectedAnswersFromDB(examId: Long): List<UserSelectedAnswer>? {
        return userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.ExamId.eq(examId)).list()
    }

    fun loadUserSelectedAnswers(examId: Long, url: String): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        clearUserSelectedAnswersFromDB(examId)
        fetchUserSelectedAnswers(url, examId)
        return resourceUserSelectedAnswers
    }

    fun getUserSelectedAnswers(examId: Long): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        val userSelectedAnswers = getUserSelectedAnswersFromDB(examId)
        if (userSelectedAnswers?.isNotEmpty() == true) {
            _resourceUserSelectedAnswers.postValue(Resource.success(userSelectedAnswers?.asDomainModels()))
        }
        return resourceUserSelectedAnswers
    }

    fun getUserSelectedAnswer(id: Long): UserSelectedAnswer? {
        return userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.Id.eq(id)).list().get(0)
    }

    fun setAnswer(id: Long, selectedOptions: ArrayList<Int>) {
        val userSelectedAnswer = getUserSelectedAnswer(id)
        userSelectedAnswer?.selectedAnswers?.clear()
        userSelectedAnswer?.selectedAnswers?.addAll(selectedOptions)
        userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer)
    }

    fun submitAnswer(id: Long): MutableLiveData<Resource<NetworkUserSelectedAnswer>> {
        val userSelectedAnswer = getUserSelectedAnswer(id)
        val answer = HashMap<String, Any>()
        answer["selected_answers"] = userSelectedAnswer!!.selectedAnswers
        userSelectedAnswer.shortText?.let {
            answer["short_text"] = it
        }
        examNetwork.saveUserSelectedAnswer(userSelectedAnswer.url!!, answer)
            .enqueue(object : TestpressCallback<NetworkUserSelectedAnswer>() {
                override fun onSuccess(result: NetworkUserSelectedAnswer?) {
                    var usa = result?.asGreenDaoModel()
                    usa?.questionId = userSelectedAnswer.questionId
                    usa?.examId = userSelectedAnswer.examId
                    userSelectedAnswerDao.insertOrReplaceInTx(usa)
                    answerResource.postValue(Resource.success(result))
                }

                override fun onException(exception: TestpressException?) {
                    answerResource.postValue(Resource.error(exception!!, null))
                }
            })
        return answerResource
    }
}