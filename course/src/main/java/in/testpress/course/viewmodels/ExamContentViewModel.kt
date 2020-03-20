package `in`.testpress.course.viewmodels

import `in`.testpress.course.domain.DomainContentAttempt
import `in`.testpress.course.domain.DomainLanguage
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.repository.ExamContentRepository
import `in`.testpress.models.greendao.CourseAttempt
import androidx.lifecycle.LiveData

class ExamContentViewModel(override val repository: ExamContentRepository) : ContentViewModel(repository) {
    fun loadContentAttempts(
        url: String,
        contentId: Long,
        forceRefresh: Boolean = false
    ): LiveData<Resource<ArrayList<DomainContentAttempt>>> {
        return repository.loadAttempts(url, contentId, forceRefresh)
    }

    fun getLanguages(examSlug: String, examId: Long): LiveData<Resource<List<DomainLanguage>>> {
        return repository.loadLanguages(examSlug, examId)
    }
}