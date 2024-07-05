package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.OfflineExamRepository
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.exam.repository.ExamRepository
import `in`.testpress.exam.ui.viewmodel.ExamViewModel
import `in`.testpress.network.Resource
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    companion object {
        fun initializeViewModel(activity: AppCompatActivity): OfflineExamViewModel {
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