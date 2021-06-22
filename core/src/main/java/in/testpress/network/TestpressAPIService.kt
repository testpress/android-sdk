package `in`.testpress.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.NetworkForum
import `in`.testpress.models.TestpressApiResponse
import android.content.Context
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap


const val URL_FORUMS_FRAG = "api/v2.3/forum/"

@JvmSuppressWildcards
interface TestpressAPIService {
    @GET(URL_FORUMS_FRAG)
    suspend fun fetchPosts(
            @QueryMap options: Map<String, Any>
    ): Response<TestpressApiResponse<NetworkForum>>
}

class APIClient(context: Context): TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {
    private fun getService() = retrofit.create(TestpressAPIService::class.java)

    suspend fun getPosts(queryParams: Map<String, Any>): Response<TestpressApiResponse<NetworkForum>> {
        return getService().fetchPosts(queryParams)
    }
}