package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.network.Resource
import `in`.testpress.course.repository.QuizExamRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class QuizExamViewModel(val repository: QuizExamRepository): ViewModel() {

    val endContentAttemptState = repository.endContentAttemptState

    fun loadContentAttempt(id: Long): LiveData<Resource<DomainContentAttempt>> {
        return repository.loadContentAttempt(id)
    }

    fun endExam(url: String, attemptId: Long) {
        repository.endExam(url, attemptId)
    }
}