package `in`.testpress.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import `in`.testpress.network.APIClient

class GlobalSearchPagingSource(
    private val apiClient: APIClient,
    private val queryParams: Map<String, Any>,
    private val filterQueryParams: List<String>
) : PagingSource<Int, SearchResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult> {
        return try {
            val mutableMap = queryParams.toMutableMap()
            mutableMap["page"] = params.key ?: 1
            mutableMap["size"] = 20
            val response = apiClient.getGlobalSearch(mutableMap, filterQueryParams)

            LoadResult.Page(
                data = response.results,
                prevKey = null,
                nextKey = response.nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, SearchResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
