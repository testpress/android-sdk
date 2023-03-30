package `in`.testpress.course.pagination

import android.util.Log
import `in`.testpress.database.dao.RunningContentDao
import `in`.testpress.database.dao.RunningContentRemoteKeysDao
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.database.entities.RunningContentRemoteKeys
import androidx.paging.*
import androidx.room.withTransaction
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.v2_4.models.ApiResponse
import retrofit2.HttpException
import java.io.IOException

private const val DEFAULT_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class RunningContentRemoteMediator(
    val courseNetwork: CourseNetwork,
    val database: TestpressDatabase,
    val courseId: Long
) : RemoteMediator<Int, RunningContentEntity>() {

    private val runningContentDao: RunningContentDao = database.runningContentDao()
    private val runningContentRemoteKeysDao: RunningContentRemoteKeysDao = database.runningContentRemoteKeysDao()
    private var pageNumber = 1

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RunningContentEntity>
    ): MediatorResult {

        pageNumber = getPageNumber(loadType,state)
        if (pageNumber == -1) return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val response = fetchRunningContents(pageNumber)

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
        state: PagingState<Int, RunningContentEntity>
    ) : Int {
        return when (loadType) {
            LoadType.REFRESH -> DEFAULT_PAGE_INDEX
            LoadType.PREPEND -> -1
            LoadType.APPEND -> getNextPageNumber(state)
        }
    }

    private suspend fun getNextPageNumber(state: PagingState<Int, RunningContentEntity>) : Int{
        val remoteKeys = getRemoteKeyForLastItem(state)
        return remoteKeys?.nextKey ?: -1
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, RunningContentEntity>): RunningContentRemoteKeys? {
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.sortedByDescending { it.start }
            ?.lastOrNull()
            ?.let { content ->
                runningContentRemoteKeysDao.remoteKeysContentId(content.id)
            }
    }

    private suspend fun fetchRunningContents(page: Int = 1): ApiResponse<List<RunningContentEntity>> {
        val queryParams = hashMapOf<String, Any>("page" to page)
        return courseNetwork.getRunningContents(courseId, queryParams)
    }

    private suspend fun storeDataInDB(
        loadType: LoadType,
        response: ApiResponse<List<RunningContentEntity>>
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
        runningContentRemoteKeysDao.clearRemoteKeysByCourseIdAndClassName(courseId)
        runningContentDao.deleteAll(courseId)
    }

    private fun generateRemoteKeys(
        response: ApiResponse<List<RunningContentEntity>>
    ):List<RunningContentRemoteKeys> {
        val prevKey = if (pageNumber == 1) null else pageNumber.minus(1)
        val nextKey = if (response.next == null) null else pageNumber.plus(1)
        return response.results.map {
            RunningContentRemoteKeys(
                contentId = it.id,
                prevKey = prevKey,
                nextKey = nextKey,
                courseId
            )
        }
    }

    private suspend fun saveData(
        results: List<RunningContentEntity>,
        keys: List<RunningContentRemoteKeys>
    ) {
        runningContentDao.insertAll(results)
        runningContentRemoteKeysDao.insertAll(keys)
    }
}