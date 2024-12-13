package `in`.testpress.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class GlobalSearchViewModel(private val repository: GlobalSearchRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow<Map<String, String>>(mapOf())
    private val filterQueryFlow = MutableStateFlow<List<String>>(listOf())

    data class SearchParams(
        val query: Map<String, String>,
        val filters: List<String>
    )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val globalSearchResults: Flow<PagingData<SearchResult>> =
        combine(
            queryFlow.debounce(300).distinctUntilChanged(),
            filterQueryFlow.debounce(300).distinctUntilChanged()
        ) { query, filters ->
            SearchParams(query, filters)
        }
            .flatMapLatest { params ->
                if (params.query.isEmpty()) {
                    flowOf(PagingData.empty())
                } else {
                    repository.getGlobalSearchResults(params.query, params.filters)
                }
            }
            .cachedIn(viewModelScope)

    fun updateSearchQuery(newQuery: Map<String, String>) {
        queryFlow.value = newQuery
    }

    fun updateFilterQuery(newFilters: List<String>) {
        filterQueryFlow.value = newFilters
    }
}


