package `in`.testpress.course.viewmodels

import androidx.lifecycle.ViewModel
import `in`.testpress.course.network.NetworkVideoQuestion


class VideoQuestionViewModel : ViewModel() {

    private var allQuestions: List<NetworkVideoQuestion> = emptyList()
    private val completedQuestionIds = mutableSetOf<Long>()

    fun setQuestions(questions: List<NetworkVideoQuestion>) {
        allQuestions = questions
        completedQuestionIds.clear()
    }

    fun getNextQuestionForPosition(position: Int): NetworkVideoQuestion? {
        return allQuestions
            .filter { it.position == position } 
            .sortedBy { it.order } 
            .firstOrNull { !completedQuestionIds.contains(it.id) } 
    }

    fun markQuestionAsCompleted(questionId: Long): Int {
        completedQuestionIds.add(questionId)
        return allQuestions.first { it.id == questionId }.position
    }

    fun getUniquePositions(): List<Int> {
        return allQuestions.map { it.position }.distinct()
    }

    fun getUniquePositionMs(): LongArray {
        return allQuestions.map { it.position * 1000L }.distinct().toLongArray()
    }
}

