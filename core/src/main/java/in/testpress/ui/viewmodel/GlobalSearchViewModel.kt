package `in`.testpress.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import `in`.testpress.models.SearchResult
import `in`.testpress.repository.GlobalSearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class GlobalSearchViewModel(private val repository: GlobalSearchRepository) : ViewModel() {

    private val queryFlow = MutableStateFlow<Map<String, String>>(mapOf())
    private val filterQueryFlow = MutableStateFlow<Pair<List<String>, List<String>>>(Pair(listOf(), listOf()))

    data class SearchParams(
        val query: Map<String, String>,
        val filters: Pair<List<String>, List<String>>
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

    fun updateFilterQuery(newFilters: Pair<List<String>, List<String>>) {
        filterQueryFlow.value = newFilters
    }

    fun hasQuery(): Boolean = queryFlow.value.isNotEmpty()
}


