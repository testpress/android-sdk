package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainAttempt
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.network.Resource
import `in`.testpress.course.repository.QuizExamRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class QuizExamViewModel(val repository: QuizExamRepository): ViewModel() {

    val endAttemptState = repository.endAttemptState

    val endContentAttemptState = repository.endContentAttemptState

    fun loadContentAttempt(id: Long): LiveData<Resource<DomainContentAttempt>> {
        return repository.loadContentAttempt(id)
    }

    fun loadAttempt(id: Long): LiveData<Resource<DomainAttempt>> {
        return repository.loadAttempt(id)
    }

    fun endExam(examId: Long, url: String, attemptId: Long) {
        if (examId == -1L) {
            endAttempt(url, attemptId)
        } else {
            endContentAttempt(url, attemptId)
        }
    }

    private fun endContentAttempt(url: String, attemptId: Long) {
        repository.endContentAttempt(url, attemptId)
    }

    private fun endAttempt(url: String, attemptId: Long) {
        repository.endAttempt(url, attemptId)
    }
}