package `in`.testpress.ui

import `in`.testpress.models.NetworkForum
import `in`.testpress.network.APIClient
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState

class DiscussionPageSource(private val apiClient: APIClient): PagingSource<Int, NetworkForum>() {
    override fun getRefreshKey(state: PagingState<Int, NetworkForum>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NetworkForum> {
        try {
            // Start refresh at page 1 if undefined.
            val nextPage: Int = params.key ?: 1
            val queryParams = hashMapOf<String, Any>("page" to nextPage)
            val response = apiClient.getPosts(queryParams).body()
            val results:List<NetworkForum> = response?.results as List<NetworkForum>
            Log.d("TAG", "load: ${nextPage}")
            return LoadResult.Page(
                    data = results,
                    prevKey = if (nextPage == 1) null else nextPage - 1,
                    nextKey = if (response?.next != null) nextPage + 1 else null
            )
        } catch (e: Exception) {
            e.stackTrace
            Log.d("TAG", "Error: ${e}")
            return LoadResult.Error(e)
        }
    }

}