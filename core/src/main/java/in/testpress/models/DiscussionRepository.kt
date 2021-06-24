package `in`.testpress.models

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.DiscussionPostEntity
import `in`.testpress.network.APIClient
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow


class DiscussionRepository(val apiClient: APIClient, val database: TestpressDatabase) {
    private fun getDefaultPageConfig(): PagingConfig {
        return PagingConfig(pageSize = 20, enablePlaceholders = false)
    }

    @ExperimentalPagingApi
    fun discussionsFlow(pagingConfig: PagingConfig = getDefaultPageConfig()): Flow<PagingData<DiscussionPostEntity>> {
        val pagingSourceFactory = { database.forumDao().getDiscussions() }
        return Pager(
                config = pagingConfig,
                pagingSourceFactory = pagingSourceFactory,
                remoteMediator = DiscussionsMediator(apiClient, database)
        ).flow
    }
}