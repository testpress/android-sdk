package `in`.testpress.exam.ui.viewmodel

import `in`.testpress.exam.models.AttemptItem
import `in`.testpress.exam.network.NetworkAttemptSection
import `in`.testpress.exam.repository.AttemptItemRepository
import `in`.testpress.exam.ui.TestFragment
import `in`.testpress.exam.ui.TestFragment.Action
import `in`.testpress.models.greendao.Attempt
import `in`.testpress.models.greendao.CourseAttempt
import `in`.testpress.network.Resource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AttemptItemViewModel(val repository: AttemptItemRepository) : ViewModel() {

    val attemptItemsResource: LiveData<Resource<List<AttemptItem>>> get() = repository.attemptItemsResource

    val saveResultResource: LiveData<Resource<Triple<Int, AttemptItem?, TestFragment.Action>>> get() = repository.saveResultResource

    val updateSectionResource: LiveData<Resource<Pair<NetworkAttemptSection?,Action>>> get() = repository.updateSectionResource

    val endContentAttemptResource: LiveData<Resource<CourseAttempt>> get() = repository.endContentAttemptResource

    val endAttemptResource: LiveData<Resource<Attempt>> get() = repository.endAttemptResource

    val resumeAttemptResource: LiveData<Resource<Attempt>> get() = repository.resumeAttemptResource

    val totalQuestions: Int get() = repository.totalQuestions

    var isNextPageQuestionsBeingFetched: Boolean = false
    var currentQuestionPosition = 0

    fun fetchAttemptItems(questionsUrlFrag: String, fetchSinglePageOnly: Boolean){
        repository.fetchAttemptItems(questionsUrlFrag, fetchSinglePageOnly)
    }

    fun saveAnswer(position: Int, attemptItem: AttemptItem, action: TestFragment.Action){
        repository.saveAnswer(position, attemptItem, action)
    }

    fun updateSection(url: String, action: TestFragment.Action){
        repository.updateSection(url, action)
    }

    fun endContentAttempt(endAttemptUrl: String) {
        repository.endContentAttempt(endAttemptUrl)
    }

    fun endAttempt(endUrlFrag: String) {
        repository.endAttempt(endUrlFrag)
    }

    fun resumeExam(startUrlFrag: String){
        repository.resumeExam(startUrlFrag)
    }

    fun clearAttemptItem() = repository.clearAttemptItem()

    fun resetPageCount() = repository.resetPageCount()

    companion object {
        fun initializeViewModel(activity: FragmentActivity): AttemptItemViewModel {
            return ViewModelProvider(activity, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AttemptItemViewModel(
                        AttemptItemRepository(activity)
                    ) as T
                }
            }).get(AttemptItemViewModel::class.java)
        }
    }
}