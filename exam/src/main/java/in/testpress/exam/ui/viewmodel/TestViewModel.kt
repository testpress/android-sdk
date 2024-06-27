package `in`.testpress.exam.ui.viewmodel

import `in`.testpress.exam.models.Permission
import `in`.testpress.exam.repository.TestRepository
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.Language
import `in`.testpress.network.Resource
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class TestViewModel(val repository: TestRepository) : ViewModel() {

    val attemptResource: LiveData<Resource<Attempt>> get() = repository.attemptResource

    val contentAttemptResource: LiveData<Resource<CourseAttempt>> get() = repository.contentAttemptResource

    val languageResource: LiveData<Resource<List<Language>>> get() = repository.languageResource

    val permissionResource: LiveData<Resource<Permission>> get() = repository.permissionResource

    fun setOfflineExam(isOfflineExam: Boolean){
        repository.isOfflineExam = isOfflineExam
    }

    fun createContentAttempt(examId: Long, attemptUrlFrag: String, queryParams: HashMap<String, Any>) {
        repository.createContentAttempt(examId, attemptUrlFrag, queryParams)
    }

    fun createAttempt(examId: Long, attemptUrlFrag: String, queryParams: HashMap<String, Any>) {
        repository.createAttempt(examId, attemptUrlFrag, queryParams)
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

    fun fetchLanguages(examSlug: String) {
        repository.fetchLanguages(examSlug)
    }

    fun checkPermission(contentId: Long) {
        repository.checkPermission(contentId)
    }

    companion object {
        fun initializeViewModel(activity: AppCompatActivity): TestViewModel {
            return ViewModelProvider(activity, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TestViewModel(
                        TestRepository(activity)
                    ) as T
                }
            }).get(TestViewModel::class.java)
        }
    }
}
