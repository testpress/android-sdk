package `in`.testpress.course.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.api.TestpressCourseApiClient.COURSE_PATH_v2_5
import `in`.testpress.course.api.TestpressCourseApiClient.RUNNING_CONTENTS_PATH
import `in`.testpress.database.entities.ProductCategoryEntity
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.exam.network.NetworkAttempt
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.Course
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.v2_4.models.ApiResponse
import `in`.testpress.v2_4.models.ContentsListResponse
import android.content.Context
import android.util.Log
import retrofit2.http.*
import java.util.HashMap

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

    @GET("{contents_url}")
    fun getContents(
        @Path(value = "contents_url", encoded = true) contentUrl: String,
        @QueryMap queryParams: HashMap<String, Any>
    ): RetrofitCall<ApiResponse<ContentsListResponse>>

    @GET(TestpressCourseApiClient.COURSE_LIST_PATH)
    fun getCourses(@QueryMap queryParams: HashMap<String, Any>): RetrofitCall<TestpressApiResponse<Course>>

    @POST(TestpressCourseApiClient.VIDEO_CONTENT_ATTEMPT_UPDATE_PATH)
    fun syncVideoWatchData(
        @Body arguments: HashMap<String, Any>
    ): RetrofitCall<Void>

    @POST("/api/v2.5/chapter_contents/{content_id}/drm_license/?download=true")
    fun getDRMLicenseURL(@Path(value = "content_id", encoded = true) contentId: Long, @Body arguments: HashMap<String, Any>): RetrofitCall<NetworkDRMLicenseAPIResult>

    @GET(TestpressCourseApiClient.V5_PRODUCTS_LIST_PATH + TestpressCourseApiClient.PRODUCTS_CATEGORIES_PATH)
    fun getProductsCategories(
        @QueryMap arguments: HashMap<String, Any>
    ): RetrofitCall<ApiResponse<List<ProductCategoryEntity>>>

    @GET("$COURSE_PATH_v2_5{course_id}$RUNNING_CONTENTS_PATH")
    fun getRunningContents(
        @Path(value = "course_id", encoded = true) courseId: Long,
        @QueryMap queryParams: HashMap<String, Any>
    ): RetrofitCall<ApiResponse<List<RunningContentEntity>>>

    @GET("$COURSE_PATH_v2_5{course_id}$UPCOMING_CONTENTS_PATH")
    fun getUpcomingContents(
        @Path(value = "course_id", encoded = true) courseId: Long,
        @QueryMap queryParams: HashMap<String, Any>,
    ): RetrofitCall<ApiResponse<List<UpcomingContentEntity>>>

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

    fun getContents(url: String, arguments: HashMap<String, Any>): RetrofitCall<ApiResponse<ContentsListResponse>> {
        return getCourseService().getContents(url, arguments)
    }

    fun getCourses(arguments: HashMap<String, Any>): RetrofitCall<TestpressApiResponse<Course>> {
        return getCourseService().getCourses(arguments)
    }

    fun syncVideoWatchData(arguments: HashMap<String, Any>): RetrofitCall<Void> {
        return getCourseService().syncVideoWatchData(arguments)
    }

    fun getDRMLicenseURL(contentId: Long): RetrofitCall<NetworkDRMLicenseAPIResult> {
        val args = hashMapOf<String, Any>("download" to true)
        return getCourseService().getDRMLicenseURL(contentId, args)
    }

    fun getRunningContents(courseId: Long, arguments: HashMap<String, Any>): RetrofitCall<ApiResponse<List<RunningContentEntity>>> {
        return getCourseService().getRunningContents(courseId, arguments)
    }

    fun getUpcomingContents(courseId: Long, arguments: HashMap<String, Any>): RetrofitCall<ApiResponse<List<UpcomingContentEntity>>> {
        return getCourseService().getUpcomingContents(courseId, arguments)
    }
}