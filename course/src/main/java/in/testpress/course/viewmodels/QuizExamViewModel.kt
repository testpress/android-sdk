package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainAttempt
import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.repository.QuizExamRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QuizExamViewModel(val repository: QuizExamRepository): ViewModel() {
    fun loadAttempt(id: Long): LiveData<Resource<DomainContentAttempt>> {
        return repository.loadAttempt(id)
    }

    fun endExam(url: String): MutableLiveData<Resource<DomainAttempt>> {
        return repository.endExam(url)
    }
}