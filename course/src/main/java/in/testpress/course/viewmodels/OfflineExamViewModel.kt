package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.OfflineExamRepository
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.network.Resource
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class OfflineExamViewModel(private val repository: OfflineExamRepository) : ViewModel() {

    val downloadExamResult: LiveData<Resource<Boolean>> get() = repository.downloadExamResult

    fun downloadExam(courseId: Long) {
        repository.downloadExam(courseId)
    }

    fun getAll(): LiveData<List<OfflineExam>>{
        return repository.getAll()
    }

    fun deleteOfflineExam(examId: Long) {
        return repository.deleteOfflineExam(examId)
    }


}