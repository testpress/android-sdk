package `in`.testpress.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.DiscussionPostEntity
import `in`.testpress.models.DiscussionRepository
import `in`.testpress.network.APIClient
import android.app.Application
import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DiscussionViewModel(
        application: Application,
        private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val service = APIClient(application)
    private val database = TestpressDatabase(application)
    private val repository = DiscussionRepository(service, database)
    val categories = repository.categories

    private val clearListChannel = Channel<Unit>(Channel.CONFLATED)
    private var _categoriesNetworkError = MutableLiveData<Boolean>(false)
    val categoriesNetworkError: LiveData<Boolean>
        get() = _categoriesNetworkError

    init {
        refreshDataFromRepository()
    }

    @ExperimentalPagingApi
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val discussions = flowOf(
            clearListChannel.receiveAsFlow().map { PagingData.empty<DiscussionPostEntity>() },
            savedStateHandle.getLiveData<HashMap<String, String>>("params")
                    .asFlow()
                    .flatMapLatest { repository.discussionsFlow(it) }
                    .cachedIn(viewModelScope)
    ).flattenMerge(2)


    fun sortAndFilter(sortBy: String, category: String = "") {
        val data = hashMapOf("sortBy" to sortBy, "category" to category)
        savedStateHandle.set("params", data)
        clearListChannel.offer(Unit)
    }

    private fun refreshDataFromRepository() {
        viewModelScope.launch {
            try {
                repository.refreshCategories()
                _categoriesNetworkError.value = false
            } catch (networkError: TestpressException) {
                if(categories.value.isNullOrEmpty())
                    _categoriesNetworkError.value = true
            }
        }
    }
}
