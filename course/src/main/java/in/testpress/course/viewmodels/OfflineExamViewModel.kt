package `in`.testpress.course.viewmodels

import `in`.testpress.core.TestpressException
import `in`.testpress.course.repository.OfflineExamRepository
import `in`.testpress.database.entities.OfflineExam
import `in`.testpress.enums.Status
import `in`.testpress.network.Resource
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class OfflineExamViewModel(private val repository: OfflineExamRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _downloadComplete = MutableLiveData<Resource<Boolean>>()
    val downloadComplete: LiveData<Resource<Boolean>> get() = _downloadComplete

    private val _isExamDownloaded = MutableLiveData<Boolean>()
    val isExamDownloaded: LiveData<Boolean> get() = _isExamDownloaded

    fun checkIfExamIsDownloaded(examId: Long) {
        viewModelScope.launch {
            _isExamDownloaded.value = repository.isExamDownloaded(examId)
        }
    }

    fun downloadExam(courseId: Long, examId: Long, examSlug: String) {
        _isLoading.value = true

        repository.downloadExam(courseId)
        repository.downloadQuestions(examId)
        repository.downloadLanguages(examId, examSlug)

        val results = mutableListOf<Resource<Boolean>>()

        repository.downloadExamResult.observeForever { examResult ->
            if (examResult != null) {
                results.add(examResult)
                checkAllResultsComplete(results)
            }
        }
        repository.downloadQuestionsResult.observeForever { questionsResult ->
            if (questionsResult != null) {
                results.add(questionsResult)
                checkAllResultsComplete(results)
            }
        }

        repository.downloadLanguagesResult.observeForever { languagesResult ->
            if (languagesResult != null) {
                results.add(languagesResult)
                checkAllResultsComplete(results)
            }
        }
    }

    private fun checkAllResultsComplete(results: List<Resource<Boolean>>) {
        if (results.size == 3) { // Assuming you have exactly 3 API calls
            val finalResult = if (results.all { it.status == Status.SUCCESS }) {
                Resource.success(true)
            } else {
                results.firstOrNull { it.status == Status.ERROR } ?: Resource.error(
                    TestpressException.unexpectedError(Exception("Download Failed")), null
                )
            }
            _downloadComplete.postValue(finalResult)
            _isLoading.postValue(false)
        }
    }

    fun getAllOfflineExams(): LiveData<List<OfflineExam>> {
        return repository.getAllOfflineExams()
    }

    fun deleteExam(examId: Long) {
        repository.deleteExam(examId)
    }
}