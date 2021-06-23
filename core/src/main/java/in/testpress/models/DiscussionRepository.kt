package `in`.testpress.models

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.ForumEntity
import `in`.testpress.network.APIClient
import `in`.testpress.ui.DiscussionPageSource
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow


class DiscussionRepository(val apiClient: APIClient, val database: TestpressDatabase) {

    fun getDefaultPageConfig(): PagingConfig {
        return PagingConfig(pageSize = 20, enablePlaceholders = false)
    }

    @ExperimentalPagingApi
    fun discussionsFlow(pagingConfig: PagingConfig = getDefaultPageConfig()): Flow<PagingData<ForumEntity>> {
        if (database == null) throw IllegalStateException("Database is not initialized")

        val pagingSourceFactory = { database.forumDao().getDiscussions() }
        return Pager(
                config = pagingConfig,
                pagingSourceFactory = pagingSourceFactory,
                remoteMediator = DiscussionsMediator(apiClient, database)
        ).flow
    }
}