package `in`.testpress.models

import `in`.testpress.network.APIClient
import `in`.testpress.ui.DiscussionPageSource
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow


class DiscussionRepository(private val testpressService: APIClient) {

    fun fetchDiscussions(): Flow<PagingData<NetworkForum>> {
        Log.d("TAG", "fetchDiscussions: ")
        return Pager(
                PagingConfig(pageSize = 40, enablePlaceholders = true)
        ) {
            DiscussionPageSource(testpressService)
        }.flow
    }
}