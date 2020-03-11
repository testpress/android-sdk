package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainLanguage
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.repository.ExamContentRepository
import `in`.testpress.models.greendao.CourseAttempt
import androidx.lifecycle.LiveData

class ExamContentViewModel(override val repository: ExamContentRepository) : ContentViewModel(repository) {
    fun loadAttempts(url: String, contentId: Long): LiveData<Resource<ArrayList<NetworkContentAttempt>>> {
        return repository.loadAttempts(url, contentId)
    }

    fun getContentAttemptsFromDB(contentId: Long): List<CourseAttempt> {
        return repository.getContentAttemptsFromDB(contentId)
    }

    fun getLanguages(examSlug: String, examId: Long): LiveData<Resource<List<DomainLanguage>>> {
        return repository.fetchLanguages(examSlug, examId)
    }
}