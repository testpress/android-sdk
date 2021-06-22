package `in`.testpress.models

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.ForumEntity
import `in`.testpress.database.entities.RemoteKeys
import `in`.testpress.network.TestpressAPIService
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator

@ExperimentalPagingApi
class DiscussionsMediator(val apiService: TestpressAPIService, val database: TestpressDatabase): RemoteMediator<Int, ForumEntity>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, ForumEntity>): MediatorResult {
        TODO("Not yet implemented")
    }

    suspend fun getFirstRemoteKey(state: PagingState<Int, ForumEntity>): RemoteKeys? {
        return state.pages.firstOrNull() {it.data.isNotEmpty()}
                ?.data?.firstOrNull()
                ?.let { forumEntity -> database.remoteKeysDao().remoteKeysDoggoId(forumEntity.id!!) }
    }

}