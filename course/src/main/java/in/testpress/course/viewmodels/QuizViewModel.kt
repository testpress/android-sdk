package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.repository.QuizQuestionsRepository
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QuizViewModel(val repository: QuizQuestionsRepository): ViewModel() {
    fun loadAttempt(contentId: Long): LiveData<Resource<DomainContentAttempt>> {
        return repository.createAttempt(contentId)
    }

    fun loadUserSelectedAnswers(examID: Long, attemptId: Long, url: String): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        return repository.getQuestions(examID, attemptId, url)
    }

    fun getUserSelectedAnswers(attemptId: Long): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        return repository.getUserSelectedAnswers(attemptId)
    }

    fun setAnswer(id: Long, selectedOptions: ArrayList<Int>) {
        repository.setAnswer(id, selectedOptions)
    }

    fun submitAnswer(id: Long): MutableLiveData<Resource<DomainUserSelectedAnswer>> {
        return repository.submitAnswer(id)
    }
}