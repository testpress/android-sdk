package `in`.testpress.course.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import android.content.Context
import retrofit2.http.GET
import retrofit2.http.Path

interface CourseService {
    @GET("{content_url}")
    fun getNetworkContent(
        @Path(value = "content_url", encoded = true) contentUrl: String
    ): RetrofitCall<NetworkContent>
}


class CourseNetwork(context: Context) : TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {
    private fun getCourseService(): CourseService {
        return retrofit.create(CourseService::class.java)
    }

    fun getNetworkContent(url: String): RetrofitCall<NetworkContent> {
        return getCourseService().getNetworkContent(url)
    }
}