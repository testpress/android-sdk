package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.OfflineExamRepository
import `in`.testpress.database.entities.OfflineAttempt
import `in`.testpress.database.entities.OfflineAttemptSection
import `in`.testpress.database.entities.OfflineCourseAttempt
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.network.Resource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class OfflineExamViewModel(private val repository: OfflineExamRepository) : ViewModel() {

    val downloadExamResult: LiveData<Resource<Boolean>> get() = repository.downloadExamResult

    fun downloadExam(courseId: Long) {
        repository.downloadExam(courseId)
    }

    fun getAll(): LiveData<List<OfflineExam>>{
        return repository.getAll()
    }

    fun get(contentId: Long): LiveData<OfflineExam?> {
        return repository.get(contentId)
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

    suspend fun getOfflineContentAttempts(attemptId: Long): OfflineCourseAttempt? {
        return repository.getOfflineContentAttempts(attemptId)
    }

    suspend fun getOfflineAttemptSectionList(attemptId: Long): List<OfflineAttemptSection> {
        return repository.getOfflineAttemptSectionList(attemptId)
    }

    suspend fun getOfflineAttemptsByExamIdAndState(examId: Long, state: String): List<OfflineAttempt> {
        return repository.getOfflineAttemptsByExamIdAndState(examId, state)
    }

    companion object {
        fun initializeViewModel(activity: FragmentActivity): OfflineExamViewModel {
            return ViewModelProvider(activity, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OfflineExamViewModel(
                        OfflineExamRepository(activity)
                    ) as T
                }
            }).get(OfflineExamViewModel::class.java)
        }
    }

}