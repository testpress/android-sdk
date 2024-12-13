package `in`.testpress.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
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


data class SearchApiResponse(
    val results: List<SearchResult>,
    val nextPage: Int?
)

data class SearchResult(
    val title: String,
    val highlight: Highlight,
    val active: Boolean,
    val type: String,
    val id: Int
)

data class Highlight(
    val title: String
)
