package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.OfflineExamRepository
import `in`.testpress.database.entities.OfflineAttempt
import `in`.testpress.database.entities.OfflineAttemptSection
import `in`.testpress.database.entities.OfflineCourseAttempt
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.network.Resource
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class OfflineExamViewModel(private val repository: OfflineExamRepository) : ViewModel() {

    val downloadExamResult: LiveData<Resource<Boolean>> get() = repository.downloadExamResult

    fun downloadExam(courseId: Long) {
        repository.downloadExam(courseId)
    }

    fun getAll(): LiveData<List<OfflineExam>>{
        return repository.getAll()
    }

    fun getOfflineExam(examId: Long): LiveData<OfflineExam?>{
        return repository.getOfflineExam(examId)
    }

    fun getOfflineAttemptsList(examId: Long) : LiveData<List<OfflineAttempt>>{
        return repository.getOfflineAttemptsList(examId)
    }

    fun deleteOfflineExam(examId: Long) {
        viewModelScope.launch {
            repository.deleteOfflineExam(examId)
        }
    }

    fun syncExamsModifiedDates() {
        viewModelScope.launch {
            repository.syncExamsModifiedDates()
        }
    }

    fun syncExam(offlineExam: OfflineExam) {
        downloadExam(offlineExam.contentId!!)
    }

    fun getOfflineContentAttemptsList(attemptId: Long): OfflineCourseAttempt? {
        return repository.getOfflineContentAttemptsList(attemptId)
    }

    fun getOfflineAttemptListList(attemptId: Long): List<OfflineAttemptSection> {
        return repository.getOfflineAttemptListList(attemptId)
    }
}