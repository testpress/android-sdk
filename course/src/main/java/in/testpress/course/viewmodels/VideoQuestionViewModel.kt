
package `in`.testpress.course.viewmodels

import androidx.lifecycle.ViewModel
import `in`.testpress.course.network.NetworkVideoQuestion


class VideoQuestionViewModel : ViewModel() {

    private var questionsMap: Map<Long, NetworkVideoQuestion> = emptyMap()
    private var questionsByPosition: Map<Int, List<NetworkVideoQuestion>> = emptyMap()
    private val completedQuestionIds = mutableSetOf<Long>()

    fun setQuestions(questions: List<NetworkVideoQuestion>) {
        questionsMap = questions.associateBy { it.id }
        questionsByPosition = questions.groupBy { it.position }
            .mapValues { (_, questionList) -> questionList.sortedBy { it.order } }
        completedQuestionIds.clear()
    }

    fun getNextQuestionForPosition(position: Int): NetworkVideoQuestion? {
        val questionsAtPosition = questionsByPosition[position] ?: return null
        return questionsAtPosition.firstOrNull { !completedQuestionIds.contains(it.id) }
    }

    fun markQuestionAsCompleted(questionId: Long): Int? {
        completedQuestionIds.add(questionId)
        return questionsMap[questionId]?.position
    }

    fun getUniquePositions(): List<Int> {
        return questionsByPosition.keys.sorted()
    }
}

