package `in`.testpress.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.NetworkCategory
import `in`.testpress.models.NetworkDiscussionThreadAnswer
import `in`.testpress.models.NetworkForum
import `in`.testpress.models.TestpressApiResponse
import android.content.Context
import `in`.testpress.repository.SearchApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap


const val URL_FORUMS_FRAG = "api/v2.5/discussions/"
const val FORUM_CATEGORIES_URL = "api/v2.3/posts/categories/"
const val GLOBAL_SEARCH_PATH = "/api/v2.5/global_search/search_results/"

@JvmSuppressWildcards
interface TestpressAPIService {
    @GET(URL_FORUMS_FRAG)
    suspend fun fetchDiscussions(
            @QueryMap options: Map<String, Any>
    ): Response<TestpressApiResponse<NetworkForum>>

    @GET("{categories_url}")
    suspend fun fetchCategories(
            @Path(value="categories_url", encoded = true) categoriesUrl: String?
    ): Response<TestpressApiResponse<NetworkCategory>>

    @GET("$URL_FORUMS_FRAG{discussion_id}/answer/")
    fun getDiscussionAnswer(
        @Path(value = "discussion_id", encoded = true) discussionId: Long
    ): RetrofitCall<NetworkDiscussionThreadAnswer>

    @GET(GLOBAL_SEARCH_PATH)
    suspend fun getGlobalSearch(
        @QueryMap queryParams: Map<String, Any>,
        @Query("param") filterQueryParams: List<String>
    ): SearchApiResponse
}

open class APIClient(context: Context): TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {
    private fun getService() = retrofit.create(TestpressAPIService::class.java)

    open suspend fun getDiscussions(queryParams: Map<String, Any>): Response<TestpressApiResponse<NetworkForum>> {
        return getService().fetchDiscussions(queryParams)
    }

    suspend fun getCategories(url: String? = null): Response<TestpressApiResponse<NetworkCategory>> {
        val apiURL = url ?: FORUM_CATEGORIES_URL
        return getService().fetchCategories(apiURL)
    }

    fun getDiscussionAnswer(discussionId: Long): RetrofitCall<NetworkDiscussionThreadAnswer> {
        return getService().getDiscussionAnswer(discussionId)
    }

    suspend fun getGlobalSearch(queryParams: Map<String, Any>, filterQueryParams: List<String>): SearchApiResponse {
        return getService().getGlobalSearch(queryParams, filterQueryParams)
    }
}