package `in`.testpress.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import `in`.testpress.models.SearchResult
import `in`.testpress.network.APIClient
import kotlinx.coroutines.flow.Flow

class GlobalSearchRepository(private val apiClient: APIClient) {

    fun getGlobalSearchResults(query: Map<String, Any>, filterQueryParams: List<String>): Flow<PagingData<SearchResult>> {
        return Pager(
            config = PagingConfig(pageSize = 15),
            pagingSourceFactory = { GlobalSearchPagingSource(apiClient, query, filterQueryParams) }
        ).flow
    }
}
