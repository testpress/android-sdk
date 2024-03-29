package `in`.testpress.models

import `in`.testpress.core.TestpressException
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.DiscussionPostEntity
import `in`.testpress.database.entities.LastLoadedPageData
import `in`.testpress.network.APIClient
import androidx.paging.*
import androidx.room.withTransaction
import retrofit2.HttpException
import java.io.IOException

@ExperimentalPagingApi
class DiscussionsMediator(val apiClient: APIClient, val database: TestpressDatabase, val params: HashMap<String, String>): RemoteMediator<Int, DiscussionPostEntity>() {
    private val discussionPostDao = database.forumDao()
    private val lastLoadedPageDataDao = database.lastLoadedPageDataDao()

    override suspend fun load(loadType: LoadType, state: PagingState<Int, DiscussionPostEntity>): MediatorResult {

        if (loadType == LoadType.PREPEND) {
            return  MediatorResult.Success(endOfPaginationReached = true)
        }
        
        val page = when(loadType) {
            LoadType.REFRESH -> 1
            LoadType.APPEND -> getNextPageNumber()
            else -> 1
        }

        return try {
            fetchFromNetworkAndSaveToDB(page, loadType)
        } catch (exception: IOException) {
            MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun fetchFromNetworkAndSaveToDB(page: Int, loadType: LoadType): MediatorResult {
        val queryParams = getQueryParams(page)
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
    }

    private suspend fun getNextPageNumber(): Int {
        val remoteKeys = lastLoadedPageDataDao.findPageDataForResource("discussions")
        return remoteKeys?.next ?: 1
    }

    private fun getQueryParams(page: Int): HashMap<String, Any> {
        val queryParams = hashMapOf<String, Any>("page" to page)
        params.forEach {
            (key, value) -> if (value.isNotEmpty()) queryParams[key] = value
        }
        return queryParams
    }

    private suspend fun saveData(page: Int, response: TestpressApiResponse<NetworkForum>?) {
        val isEndOfList = response?.next === null
        val prevKey = if (page == 1) null else page - 1
        val nextKey = if (isEndOfList) null else page + 1
        val key = LastLoadedPageData(resourceType = "discussions", previous = prevKey, next = nextKey)
        lastLoadedPageDataDao.insert(key)
        response?.results?.asDatabaseModels()?.let { discussionPostDao.insertAll(it) }
    }

    private suspend fun clearExistingData() {
        lastLoadedPageDataDao.deleteForResource("discussions")
        discussionPostDao.deleteAll()
    }
}