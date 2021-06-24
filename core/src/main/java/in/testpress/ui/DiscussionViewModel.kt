package `in`.testpress.ui

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.DiscussionPostEntity
import `in`.testpress.models.DiscussionRepository
import `in`.testpress.network.APIClient
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class DiscussionViewModel(application: Application) : AndroidViewModel(application) {
    private val service = APIClient(application)
    private val database = TestpressDatabase(application)
    private val repository = DiscussionRepository(service, database)

    @ExperimentalPagingApi
    fun fetchPosts(): Flow<PagingData<DiscussionPostEntity>> {
        return repository.discussionsFlow().cachedIn(viewModelScope)
    }
}

class DiscussionViewModelFactory(
        private val application: Application
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            DiscussionViewModel(application) as T
}