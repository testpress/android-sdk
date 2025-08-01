package `in`.testpress.exam.ui.viewmodel

import `in`.testpress.exam.models.AttemptItem
import `in`.testpress.exam.network.NetworkAttemptSection
import `in`.testpress.exam.repository.AttemptRepository
import `in`.testpress.exam.ui.TestFragment
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.models.greendao.Exam
import `in`.testpress.network.Resource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AttemptViewModel(val repository: AttemptRepository) : ViewModel() {

    val attemptItemsResource: LiveData<Resource<List<AttemptItem>>> get() = repository.attemptItemsResource

    val saveResultResource: LiveData<Resource<Triple<Int, AttemptItem?, TestFragment.Action>>> get() = repository.saveResultResource

    val updateSectionResource: LiveData<Resource<Pair<NetworkAttemptSection?, TestFragment.Action>>> get() = repository.updateSectionResource
    val endContentAttemptResource: LiveData<Resource<CourseAttempt>> get() = repository.endContentAttemptResource
    val endAttemptResource: LiveData<Resource<Attempt>> get() = repository.endAttemptResource

    val totalQuestions: Int get() = repository.totalQuestions

    var isNextPageQuestionsBeingFetched: Boolean = false
    var currentQuestionPosition = 0

    fun setExamAndAttempt(exam: Exam?, attempt: Attempt){
        repository.exam = exam
        repository.attempt = attempt
    }

    fun fetchAttemptItems(questionsUrlFrag: String, fetchSinglePageOnly: Boolean){
        repository.fetchAttemptItems(questionsUrlFrag, fetchSinglePageOnly)
    }

    fun saveAnswer(position: Int, attemptItem: AttemptItem, action: TestFragment.Action, remainingTime: String){
        viewModelScope.launch {
            repository.saveAnswer(position, attemptItem, action, remainingTime)
        }
    }

    fun updateSection(url: String, action: TestFragment.Action){
        repository.updateSection(url, action)
    }

    fun endContentAttempt(attemptEndFrag: String, isExamWindowViolated: Boolean) {
        repository.endContentAttempt(attemptEndFrag, isExamWindowViolated)
    }

    fun endAttempt(attemptEndFrag: String) {
        repository.endAttempt(attemptEndFrag)
    }

    fun clearAttemptItems() = repository.clearAttemptItems()

    fun resetPageCount() = repository.resetPageCount()

    companion object {
        fun initializeViewModel(activity: FragmentActivity): AttemptViewModel {
            return ViewModelProvider(activity, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AttemptViewModel(
                        AttemptRepository(activity)
                    ) as T
                }
            }).get(AttemptViewModel::class.java)
        }
    }
}