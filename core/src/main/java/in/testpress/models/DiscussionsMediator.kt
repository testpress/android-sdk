package `in`.testpress.models

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.ForumEntity
import `in`.testpress.database.entities.RemoteKeys
import `in`.testpress.network.APIClient
import android.util.Log
import androidx.paging.*
import androidx.room.withTransaction
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException

@ExperimentalPagingApi
class DiscussionsMediator(val apiClient: APIClient, val database: TestpressDatabase): RemoteMediator<Int, ForumEntity>() {
    private val forumDao = database.forumDao()
    private val remoteKeysDao = database.remoteKeysDao()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, ForumEntity>): MediatorResult {

        val pageKeyData = getKeyPageData(loadType, state)
        Log.d("TAG", "load: pageKeyData : ${state.pages}")

        val page = when (pageKeyData) {
            is MediatorResult.Success -> {
                return pageKeyData
            }
            else -> {
                pageKeyData as Int
            }
        }

        try {
            // Start refresh at page 1 if undefined.
            val nextPage: Int = page
            val queryParams = hashMapOf<String, Any>("page" to nextPage)
            val response = apiClient.getDiscussions(queryParams).body()
            val isEndOfList = response?.next === null
            Log.d("TAG", "load: fetchingPage : ${nextPage} ${isEndOfList}")

            database.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    Log.d("TAG", "load: Refreshing keys")
                    remoteKeysDao.deleteAll()
                    forumDao.deleteAll()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (isEndOfList) null else page + 1
                val keys = response?.results?.map {
                    RemoteKeys(repoId = it.id!!, prevKey = prevKey, nextKey = nextKey)
                }
                Log.d("TAG", "load: Keys : ${keys}")
                if (keys != null) {
                    remoteKeysDao.insertAll(keys)
                }
                response?.results?.asDatabaseModels()?.let { forumDao.insertAll(it) }
            }

            return MediatorResult.Success(endOfPaginationReached = isEndOfList)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }


    private suspend fun getKeyPageData(loadType: LoadType, state: PagingState<Int, ForumEntity>): Any? {
        Log.d("TAG", "getKeyPageData: ${loadType}")
        return when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getClosestRemoteKey(state)
                Log.d("TAG", "getKeyPageData 1: ${remoteKeys?.nextKey?.minus(1)}")
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
//                end of list condition reached
                remoteKeys.prevKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                remoteKeys.prevKey
            }
        }
    }

    private suspend fun getFirstRemoteKey(state: PagingState<Int, ForumEntity>): RemoteKeys? {
        val a = state.pages.firstOrNull() {it.data.isNotEmpty()}
                ?.data?.firstOrNull()
                ?.let { forumEntity -> remoteKeysDao.remoteKeysDoggoId(forumEntity.id!!) }
        Log.d("TAG", "getFirstRemoteKey: ${a}")
        return a
    }

    private suspend fun getLastRemoteKey(state: PagingState<Int, ForumEntity>): RemoteKeys? {
        Log.d("TAG", "getLastRemoteKey: ${state.pages}")
        val a = state.pages
                .lastOrNull { it.data.isNotEmpty() }
                ?.data?.lastOrNull()
                ?.let { forumEntity -> remoteKeysDao.remoteKeysDoggoId(forumEntity.id!!) }
        return a
    }

    private suspend fun getClosestRemoteKey(state: PagingState<Int, ForumEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                remoteKeysDao.remoteKeysDoggoId(repoId)
            }
        }
    }

}