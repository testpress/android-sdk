package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.repository.QuizQuestionsRepository
import `in`.testpress.course.repository.UserSelectedAnswersRepository
import `in`.testpress.exam.domain.DomainUserSelectedAnswer
import `in`.testpress.exam.models.AttemptItem
import `in`.testpress.exam.network.NetworkAttempt
import `in`.testpress.exam.network.NetworkUserSelectedAnswer
import `in`.testpress.models.greendao.ExamQuestion
import `in`.testpress.models.greendao.UserSelectedAnswer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QuizViewModel(val repository: UserSelectedAnswersRepository): ViewModel() {
    // fun getQuestions(examId: Long, url: String): LiveData<Resource<List<ExamQuestion>>> {
    //     return repository.getQuestions(examId, url)
    // }

    fun loadAttempt(contentId: Long): LiveData<Resource<DomainContentAttempt>> {
        return repository.createAttempt(contentId)
    }

    fun loadUserSelectedAnswers(examId: Long, url: String) {
        repository.loadUserSelectedAnswers(examId, url)
    }

    fun getUserSelectedAnswers(examId: Long): LiveData<Resource<List<DomainUserSelectedAnswer>>> {
        return repository.getUserSelectedAnswers(examId)
    }

    fun setAnswer(id: Long, selectedOptions: ArrayList<Int>) {
        repository.setAnswer(id, selectedOptions)
    }

    fun submitAnswer(id: Long): MutableLiveData<Resource<NetworkUserSelectedAnswer>> {
        return repository.submitAnswer(id)
    }
}