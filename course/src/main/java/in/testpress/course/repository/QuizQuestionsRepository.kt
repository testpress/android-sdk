package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.exam.network.ExamNetwork
import `in`.testpress.exam.network.NetworkAnswer
import `in`.testpress.exam.network.NetworkExamQuestionResult
import `in`.testpress.exam.network.asGreenDaoModels
import `in`.testpress.models.greendao.ExamQuestion
import `in`.testpress.models.greendao.ExamQuestionDao
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class QuizQuestionsRepository(context: Context): QuizExamRepository(context) {
    private val examNetwork = ExamNetwork(context)
    val examQuestionDao = TestpressSDKDatabase.getExamQuestionDao(context)

    var _resourceQuestions: MutableLiveData<Resource<List<ExamQuestion>>> = MutableLiveData()
    val resourceQuestions: LiveData<Resource<List<ExamQuestion>>>
        get() = _resourceQuestions

    private fun fetchQuestions(url: String, examId: Long) {
        examNetwork.getQuestions(url)
            .enqueue(object: TestpressCallback<ApiResponse<NetworkExamQuestionResult>>(){
                override fun onSuccess(result: ApiResponse<NetworkExamQuestionResult>?) {
                    handleQuestionsFetchSuccess(result, examId)
                }

                override fun onException(exception: TestpressException?) {
                    _resourceQuestions.postValue(Resource.error(exception!!, null))
                }
            })
    }

    private fun handleQuestionsFetchSuccess(response: ApiResponse<NetworkExamQuestionResult>?, examId: Long) {
        if (response?.next != null) {
            fetchQuestions(response.next, examId)
        } else {
            cleanQuestionInDB(examId)
            saveQuestionsToDB(response?.results)
            val questions = getQuestionsFromDB(examId)
            _resourceQuestions.postValue(Resource.success(questions))
        }
    }

    private fun cleanQuestionInDB(examId: Long) {
        examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.ExamId.eq(examId))
            .buildDelete().executeDeleteWithoutDetachingEntities()
    }

    private fun getQuestionsFromDB(examId: Long): List<ExamQuestion>? {
        return examQuestionDao.queryBuilder().where(ExamQuestionDao.Properties.ExamId.eq(examId)).list()
    }

    private fun saveQuestionsToDB(response: NetworkExamQuestionResult?) {
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

        examQuestionDao.insertOrReplaceInTx(response?.examQuestions?.asGreenDaoModels())
        directionDao.insertOrReplaceInTx(response?.directions?.asGreenDaoModels())
    }

    fun getQuestions(examId: Long, url: String): LiveData<Resource<List<ExamQuestion>>> {
        if(getQuestionsFromDB(examId)?.isNotEmpty() == true) {
            _resourceQuestions.postValue(Resource.success(getQuestionsFromDB(examId)))
            fetchQuestions(url, examId)
        } else {
            fetchQuestions(url, examId)
        }
        return resourceQuestions
    }


}