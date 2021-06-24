package `in`.testpress.models

import `in`.testpress.core.TestpressException
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.DiscussionPostEntity
import `in`.testpress.database.entities.RemoteKeys
import `in`.testpress.network.APIClient
import androidx.paging.*
import androidx.room.withTransaction
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException

@ExperimentalPagingApi
class DiscussionsMediator(val apiClient: APIClient, val database: TestpressDatabase): RemoteMediator<Int, DiscussionPostEntity>() {
    private val discussionPostDao = database.forumDao()
    private val remoteKeysDao = database.remoteKeysDao()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, DiscussionPostEntity>): MediatorResult {

        val page = when (val pageKeyData = getKeyPageData(loadType, state)) {
            is MediatorResult.Success -> {
                return pageKeyData
            }
            else -> {
                pageKeyData as Int
            }
        }

        try {
            val queryParams = hashMapOf<String, Any>("page" to page)
            val response = apiClient.getDiscussions(queryParams)
            val responseBody = response.body()

            if (!response.isSuccessful) {
                return MediatorResult.Error(TestpressException.httpError(response))
            }

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    clearExistingData()
                }
                saveData(page, responseBody)
            }

            return MediatorResult.Success(endOfPaginationReached = responseBody?.next === null)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun saveData(page: Int, response: TestpressApiResponse<NetworkForum>?) {
        val isEndOfList = response?.next === null
        val prevKey = if (page == 1) null else page - 1
        val nextKey = if (isEndOfList) null else page + 1
        val keys = response?.results?.map {
            RemoteKeys(repoId = it.id!!, prevKey = prevKey, nextKey = nextKey)
        }
        if (keys != null) {
            remoteKeysDao.insertAll(keys)
        }
        response?.results?.asDatabaseModels()?.let { discussionPostDao.insertAll(it) }
    }

    private suspend fun clearExistingData() {
        remoteKeysDao.deleteAll()
        discussionPostDao.deleteAll()
    }


    private suspend fun getKeyPageData(loadType: LoadType, state: PagingState<Int, DiscussionPostEntity>): Any? {
        return when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getClosestRemoteKey(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.APPEND -> {
                val remoteKeys = getLastRemoteKey(state)
                        ?: throw InvalidObjectException("Remote key should not be null for $loadType")
                remoteKeys.nextKey
            }
            LoadType.PREPEND -> {
                val remoteKeys = getFirstRemoteKey(state)
                        ?: throw InvalidObjectException("Invalid state, key should not be null")
                remoteKeys.prevKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                remoteKeys.prevKey
            }
        }
    }

    private suspend fun getFirstRemoteKey(state: PagingState<Int, DiscussionPostEntity>): RemoteKeys? {
        return state.pages.firstOrNull() {it.data.isNotEmpty()}
                ?.data?.firstOrNull()
                ?.let { forumEntity -> remoteKeysDao.findKeyForDiscussion(forumEntity.id!!) }
    }

    private suspend fun getLastRemoteKey(state: PagingState<Int, DiscussionPostEntity>): RemoteKeys? {
        return state.pages
                .lastOrNull { it.data.isNotEmpty() }
                ?.data?.lastOrNull()
                ?.let { forumEntity -> remoteKeysDao.findKeyForDiscussion(forumEntity.id!!) }
    }

    private suspend fun getClosestRemoteKey(state: PagingState<Int, DiscussionPostEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                remoteKeysDao.findKeyForDiscussion(repoId)
            }
        }
    }

}