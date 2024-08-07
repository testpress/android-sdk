package `in`.testpress.models

import  `in`.testpress.core.TestpressException
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.DiscussionPostEntity
import `in`.testpress.database.entities.asDomainModels
import `in`.testpress.network.APIClient
import `in`.testpress.network.FORUM_CATEGORIES_URL
import androidx.lifecycle.map
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException


class DiscussionRepository(val apiClient: APIClient, val database: TestpressDatabase) {
    private val categoryDao = database.categoryDao()
    val categories = categoryDao.getAll().map {
        it.asDomainModels()
    }

    private fun getDefaultPageConfig(): PagingConfig {
        return PagingConfig(pageSize = 20, enablePlaceholders = false)
    }

    @ExperimentalPagingApi
    fun discussionsFlow(query: HashMap<String, String>, pagingConfig: PagingConfig = getDefaultPageConfig()): Flow<PagingData<DiscussionPostEntity>> {
        val pagingSourceFactory = { when(query.get("sort")) {
            "-upvotes" -> database.forumDao().getDiscussionsOrderedByUpvotes()
            "views_count" -> database.forumDao().getDiscussionsOrderedByViews()
            "created" -> database.forumDao().getOldestDiscussions()
            else -> database.forumDao().getDiscussionsOrderedByLatest()
        }}


        return Pager(
                config = pagingConfig,
                pagingSourceFactory = pagingSourceFactory,
                remoteMediator = DiscussionsMediator(apiClient, database, query)
        ).flow
    }

    suspend fun refreshCategories(url: String = FORUM_CATEGORIES_URL) {
        withContext(Dispatchers.IO) {

            try {
                val response = apiClient.getCategories(url)

                if (response.isSuccessful) {
                    val responseBody = saveCategories(response)

                    if (responseBody?.hasMore() == true) {
                        refreshCategories(responseBody.next)
                    }
                } else {
                    if (response.errorBody() is IOException) {
                        throw TestpressException.networkError(response.errorBody() as IOException?)
                    } else {
                        throw TestpressException.unexpectedError(IOException())
                    }
                }
            } catch (e: IOException) {
                throw TestpressException.networkError(IOException())
            }
        }
    }

    private suspend fun saveCategories(response: Response<TestpressApiResponse<NetworkCategory>>): TestpressApiResponse<NetworkCategory>? {
        val responseBody = response.body()
        val categories = responseBody?.results
        if (responseBody?.previous == null) {
            categoryDao.deleteAll()
        }
        categoryDao.insertAll(categories?.asDatabaseModels() ?: arrayListOf())
        return responseBody
    }

}