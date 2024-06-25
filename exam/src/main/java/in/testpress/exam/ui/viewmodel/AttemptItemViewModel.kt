package `in`.testpress.exam.ui.viewmodel

import `in`.testpress.exam.models.AttemptItem
import `in`.testpress.exam.repository.AttemptItemRepository
import `in`.testpress.exam.ui.TestFragment
import `in`.testpress.network.Resource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AttemptItemViewModel(val repository: AttemptItemRepository) : ViewModel() {

    val attemptItemsResource: LiveData<Resource<List<AttemptItem>>> get() = repository.attemptItemsResource

    val saveResultResource: LiveData<Resource<Triple<Int, AttemptItem?, TestFragment.Action>>> get() = repository.saveResultResource

    val totalQuestions: Int get() = repository.totalQuestions

    var isNextPageQuestionsBeingFetched: Boolean = false
    var currentQuestionPosition = 0

    fun fetchAttemptItems(questionsUrlFrag: String, fetchSinglePageOnly: Boolean){
        repository.fetchAttemptItems(questionsUrlFrag, fetchSinglePageOnly)
    }

    fun saveAnswer(position: Int, attemptItem: AttemptItem, action: TestFragment.Action){
        repository.saveAnswer(position, attemptItem, action)
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