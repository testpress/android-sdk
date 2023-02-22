package `in`.testpress.course.pagination

import `in`.testpress.course.repository.RunningContentsRepository
import `in`.testpress.database.dao.RunningContentDao
import `in`.testpress.database.dao.RunningContentRemoteKeysDao
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.database.entities.RunningContentRemoteKeys
import androidx.paging.*
import androidx.room.withTransaction
import retrofit2.HttpException
import java.io.IOException

private const val DEFAULT_PAGE_INDEX = 1

@OptIn(ExperimentalPagingApi::class)
class RunningContentRemoteMediator(
    val repository: RunningContentsRepository
) : RemoteMediator<Int, RunningContentEntity>() {
    private val db = repository.db
    private val runningContentDao: RunningContentDao = db.runningContentDao()
    private val runningContentRemoteKeysDao: RunningContentRemoteKeysDao = db.runningContentRemoteKeysDao()

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

            val response = repository.fetchRunningContents(page)

            val repos = response.results
            val endOfPaginationReached = (response.next == null)
            db.withTransaction {
                // clear all data in the database
                if (loadType == LoadType.REFRESH) {
                    runningContentRemoteKeysDao.clearRemoteKeysByCourseIdAndClassName(
                        repository.courseId
                    )
                    runningContentDao.deleteAll(repository.courseId)
                }
                val prevKey = if (page == 1) null else page.minus(1)
                val nextKey = if (endOfPaginationReached) null else page.plus(1)
                val keys = repos.map {
                    RunningContentRemoteKeys(
                        contentId = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey,
                        repository.courseId
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

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, RunningContentEntity>): RunningContentRemoteKeys? {
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.sortedByDescending { it.start }
            ?.lastOrNull()
            ?.let { content ->
                runningContentRemoteKeysDao.remoteKeysContentId(content.id)
            }
    }
}