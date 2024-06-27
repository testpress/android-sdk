package `in`.testpress.exam.ui.viewmodel

import `in`.testpress.exam.repository.ExamRepository
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.network.Resource
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class ExamViewModel(val repository: ExamRepository) : ViewModel() {

    val attemptResource: LiveData<Resource<Attempt>> get() = repository.attemptResource

    val contentAttemptResource: LiveData<Resource<CourseAttempt>> get() = repository.contentAttemptResource

    fun createContentAttempt(attemptUrlFrag: String, queryParams: HashMap<String, Any>) {
        repository.createContentAttempt(attemptUrlFrag, queryParams)
    }

    fun createAttempt(attemptUrlFrag: String, queryParams: HashMap<String, Any>) {
        repository.createAttempt(attemptUrlFrag, queryParams)
    }

    fun startAttempt(attemptStartFrag: String) {
        repository.startAttempt(attemptStartFrag)
    }

    fun endContentAttempt(attemptEndFrag: String) {
        repository.endContentAttempt(attemptEndFrag)
    }

    fun endAttempt(attemptEndFrag: String) {
        repository.endAttempt(attemptEndFrag)
    }

    companion object {
        fun initializeViewModel(activity: AppCompatActivity): ExamViewModel {
            return ViewModelProvider(activity, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ExamViewModel(
                        ExamRepository(activity)
                    ) as T
                }
            }).get(ExamViewModel::class.java)
        }
    }
}
