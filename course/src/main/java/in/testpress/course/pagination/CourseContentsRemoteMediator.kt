package `in`.testpress.course.pagination

import `in`.testpress.database.dao.ContentLiteDao
import `in`.testpress.database.dao.ContentLiteRemoteKeyDao
import `in`.testpress.database.entities.ContentEntityLite
import `in`.testpress.database.entities.ContentEntityLiteRemoteKey
import androidx.paging.*
import androidx.room.withTransaction
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.v2_4.models.ApiResponse
import retrofit2.HttpException
import java.io.IOException

private const val DEFAULT_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class CourseContentsRemoteMediator(
    val courseNetwork: CourseNetwork,
    val database: TestpressDatabase,
    val courseId: Long
) : RemoteMediator<Int, ContentEntityLite>() {

    private val contentLiteDao: ContentLiteDao = database.contentLiteDao()
    private val contentLiteRemoteKeyDao: ContentLiteRemoteKeyDao = database.contentLiteRemoteKeyDao()
    private var pageNumber = 1

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ContentEntityLite>
    ): MediatorResult {

        pageNumber = getPageNumber(loadType,state)
        if (pageNumber == -1) return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val response = fetchCourseContents(pageNumber)

            storeDataInDB(loadType,response)

            MediatorResult.Success(endOfPaginationReached = response.next == null)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getPageNumber(
        loadType: LoadType,
        state: PagingState<Int, ContentEntityLite>
    ) : Int {
        return when (loadType) {
            LoadType.REFRESH -> DEFAULT_PAGE_INDEX
            LoadType.PREPEND -> -1
            LoadType.APPEND -> getNextPageNumber(state)
        }
    }

    private suspend fun getNextPageNumber(state: PagingState<Int, ContentEntityLite>) : Int{
        val remoteKeys = getRemoteKeyForLastItem(state)
        return remoteKeys?.nextKey ?: -1
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ContentEntityLite>): ContentEntityLiteRemoteKey? {
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.sortedByDescending { it.start }
            ?.lastOrNull()
            ?.let { content ->
                contentLiteRemoteKeyDao.remoteKeysContentId(content.id)
            }
    }

    private suspend fun fetchCourseContents(page: Int = 1): ApiResponse<List<ContentEntityLite>> {
        val queryParams = hashMapOf<String, Any>("page" to page)
        return courseNetwork.getRunningContents(courseId, queryParams)
    }

    private suspend fun storeDataInDB(
        loadType: LoadType,
        response: ApiResponse<List<ContentEntityLite>>
    ){
        database.withTransaction {
            // clear all data in the database
            if (loadType == LoadType.REFRESH) {
                clearExistingData(courseId)
            }
            val keys = generateRemoteKeys(response)
            saveData(response.results,keys)
        }
    }

    private suspend fun clearExistingData(courseId: Long){
        contentLiteRemoteKeyDao.clearRemoteKeysByCourseIdAndClassName(courseId)
        contentLiteDao.deleteAll(courseId)
    }

    private fun generateRemoteKeys(
        response: ApiResponse<List<ContentEntityLite>>
    ):List<ContentEntityLiteRemoteKey> {
        val prevKey = if (pageNumber == 1) null else pageNumber.minus(1)
        val nextKey = if (response.next == null) null else pageNumber.plus(1)
        return response.results.map {
            ContentEntityLiteRemoteKey(
                contentId = it.id,
                prevKey = prevKey,
                nextKey = nextKey,
                courseId
            )
        }
    }

    private suspend fun saveData(
        results: List<ContentEntityLite>,
        keys: List<ContentEntityLiteRemoteKey>
    ) {
        contentLiteDao.insertAll(results)
        contentLiteRemoteKeyDao.insertAll(keys)
    }
}