package `in`.testpress.course.repository

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.models.greendao.ExamQuestion
import `in`.testpress.models.greendao.Question
import `in`.testpress.models.greendao.UserSelectedAnswer
import `in`.testpress.models.greendao.UserSelectedAnswerDao
import `in`.testpress.util.IntegerList
import android.content.Context
import java.util.HashMap
import kotlin.random.Random

class UserSelectedAnswersHandler(val context: Context) {
    private val userSelectedAnswerDao = TestpressSDKDatabase.getUserSelectedAnswerDao(context)

    fun getForAttempt(attemptId: Long): MutableList<UserSelectedAnswer>? {
        return userSelectedAnswerDao.queryBuilder()
            .where(UserSelectedAnswerDao.Properties.AttemptId.eq(attemptId)).list()
    }

    fun get(id: Long): UserSelectedAnswer? {
        return userSelectedAnswerDao.queryBuilder().where(UserSelectedAnswerDao.Properties.Id.eq(id)).list()[0]
    }

    fun getDataForSubmission(id: Long): HashMap<String, Any> {
        val userSelectedAnswer = get(id)
        val answer = HashMap<String, Any>()
        answer["selected_answers"] = userSelectedAnswer!!.selectedAnswers ?: listOf<Int>()
        userSelectedAnswer.shortText?.let {
            answer["short_text"] = it
        }
        return answer
    }

    fun restore(id: Long, userSelectedAnswer: UserSelectedAnswer?) {
        val userSelectedAnswerFromDB = get(id)
        userSelectedAnswer?.questionId = userSelectedAnswerFromDB?.questionId
        userSelectedAnswer?.attemptId = userSelectedAnswerFromDB?.attemptId
        userSelectedAnswer?.explanationHtml = userSelectedAnswerFromDB?.explanationHtml
        userSelectedAnswer?.correctAnswers = userSelectedAnswerFromDB?.correctAnswers
        userSelectedAnswer?.duration = "00:00:01"
        userSelectedAnswerDao.delete(userSelectedAnswerFromDB)
        userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer)
    }

    fun setSelectedOptions(id: Long, selectedOptions: ArrayList<Int>) {
        val userSelectedAnswer = get(id)
        userSelectedAnswer?.selectedAnswers = IntegerList()
        userSelectedAnswer?.selectedAnswers?.addAll(selectedOptions)
        userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer)
    }

    fun create(attemptId: Long, questions: List<ExamQuestion>) {
        val userSelectedAnswerDao = TestpressSDKDatabase.getUserSelectedAnswerDao(context)
        val id = getRandomIdForUserSelectedAnswer(questions.size)

        questions.forEachIndexed{index, examQuestion ->
            val question = examQuestion.question
            val url = "/api/v2.4/attempts/${attemptId}/questions/${examQuestion.id}/"
            val userSelectedAnswer = UserSelectedAnswer(
                id + index, index, false, null, attemptId,
                question.explanationHtml, null, null, null,
                getCorrectAnswerIdsForQuestion(question), url, question.id, examQuestion.id
            )
            userSelectedAnswerDao.insertOrReplaceInTx(userSelectedAnswer)
        }
    }

    private fun getCorrectAnswerIdsForQuestion(question: Question): IntegerList {
        val correctAnswers = question.answers.filter { it.isCorrect ?: false }
        val correctAnswersIds = IntegerList()
        correctAnswersIds.addAll(correctAnswers.map {it.id.toInt()})
        return correctAnswersIds
    }

    private fun getRandomIdForUserSelectedAnswer(endIndex: Int): Long {
        val id = Random.nextLong(99999, 9999999)
        val count = userSelectedAnswerDao.queryBuilder()
            .where(UserSelectedAnswerDao.Properties.Id.between(id, id + endIndex)).count()
        if (count > 0) {
            getRandomIdForUserSelectedAnswer(endIndex)
        }
        return id
    }
}