package `in`.testpress.course.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.exam.network.NetworkAttempt
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import android.content.Context
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url

interface CourseService {
    @GET("{content_url}")
    fun getNetworkContent(
        @Path(value = "content_url", encoded = true) contentUrl: String
    ): RetrofitCall<NetworkContent>

    @POST(TestpressCourseApiClient.CONTENTS_PATH + "{content_id}" + TestpressCourseApiClient.ATTEMPTS_PATH)
    fun createContentAttempt(
        @Path(value = "content_id", encoded = true) contentId: Long
    ): RetrofitCall<NetworkContentAttempt>

    @GET
    fun getContentAttempts(@Url attemptsUrl: String): RetrofitCall<TestpressApiResponse<NetworkContentAttempt>>

    @PUT("{end_exam_url}")
    fun endContentAttempt(
        @Path(value = "end_exam_url", encoded = true) endExamUrlFrag: String?
    ): RetrofitCall<NetworkAttempt>
}


class CourseNetwork(context: Context) : TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {
    private fun getCourseService() = retrofit.create(CourseService::class.java)

    fun getNetworkContent(url: String): RetrofitCall<NetworkContent> {
        return getCourseService().getNetworkContent(url)
    }

    fun createContentAttempt(contentId: Long): RetrofitCall<NetworkContentAttempt> {
        return getCourseService().createContentAttempt(contentId)
    }

    fun getContentAttempts(url: String): RetrofitCall<TestpressApiResponse<NetworkContentAttempt>> {
        return getCourseService().getContentAttempts(url)
    }

    fun endContentAttempt(url: String):  RetrofitCall<NetworkAttempt> {
        return getCourseService().endContentAttempt(url)
    }
}