package `in`.testpress.course.pagination

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

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RunningContentEntity>
    ): MediatorResult {


        try {
            val page = when (loadType) {
                LoadType.REFRESH -> DEFAULT_PAGE_INDEX
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextKey
                }
            }

            val response = fetchRunningContents(page)

            val repos = response.results
            val endOfPaginationReached = (response.next == null)
            database.withTransaction {
                // clear all data in the database
                if (loadType == LoadType.REFRESH) {
                    runningContentRemoteKeysDao.clearRemoteKeysByCourseIdAndClassName(courseId)
                    runningContentDao.deleteAll(courseId)
                }
                val prevKey = if (page == 1) null else page.minus(1)
                val nextKey = if (endOfPaginationReached) null else page.plus(1)
                val keys = repos.map {
                    RunningContentRemoteKeys(
                        contentId = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey,
                        courseId
                    )
                }
                runningContentRemoteKeysDao.insertAll(keys)
                runningContentDao.insertAll(repos)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun fetchRunningContents(page: Int = 1): ApiResponse<List<RunningContentEntity>> {
        val queryParams = hashMapOf<String, Any>("page" to page)
        return courseNetwork.getRunningContents(courseId, queryParams)
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, RunningContentEntity>): RunningContentRemoteKeys? {
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.sortedByDescending { it.start }
            ?.lastOrNull()
            ?.let { content ->
                runningContentRemoteKeysDao.remoteKeysContentId(content.id)
            }
    }
}