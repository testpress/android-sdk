package `in`.testpress.course.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.api.TestpressCourseApiClient
import `in`.testpress.course.api.TestpressCourseApiClient.*
import `in`.testpress.database.entities.*
import `in`.testpress.exam.api.TestpressExamApiClient
import `in`.testpress.exam.network.NetworkAttempt
import `in`.testpress.exam.network.NetworkExamContent
import `in`.testpress.exam.network.NetworkLanguage
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.Course
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.v2_4.models.ApiResponse
import `in`.testpress.v2_4.models.ContentsListResponse
import android.content.Context
import retrofit2.http.*

interface CourseService {
    @GET("{content_url}")
    fun getNetworkContent(
        @Path(value = "content_url", encoded = true) contentUrl: String
    ): RetrofitCall<NetworkContent>

    @POST(TestpressCourseApiClient.CONTENTS_PATH + "{content_id}" + TestpressCourseApiClient.ATTEMPTS_PATH)
    fun createContentAttempt(
        @Path(value = "content_id", encoded = true) contentId: Long,
        @Body queryParams: HashMap<String, Int>
    ): RetrofitCall<NetworkContentAttempt>

    @GET
    fun getContentAttempts(@Url attemptsUrl: String): RetrofitCall<TestpressApiResponse<NetworkContentAttempt>>

    @PUT("{end_exam_url}")
    fun endAttempt(
        @Path(value = "end_exam_url", encoded = true) endExamUrlFrag: String?
    ): RetrofitCall<NetworkAttempt>

    @PUT("{end_exam_url}")
    fun endContentAttempt(
        @Path(value = "end_exam_url", encoded = true) endExamUrlFrag: String?
    ): RetrofitCall<NetworkContentAttempt>

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

    @GET(V5_PRODUCTS_LIST_PATH + PRODUCTS_CATEGORIES_PATH)
    fun getProductsCategories(
        @QueryMap arguments: HashMap<String, Any>
    ): RetrofitCall<ApiResponse<List<ProductCategoryEntity>>>

    @GET("$COURSE_PATH_v2_5{course_id}$RUNNING_CONTENTS_PATH")
    suspend fun getRunningContents(
        @Path(value = "course_id", encoded = true) courseId: Long,
        @QueryMap queryParams: HashMap<String, Any>
    ): ApiResponse<List<ContentEntityLite>>

    @GET("$COURSE_PATH_v2_5{course_id}$UPCOMING_CONTENTS_PATH")
    suspend fun getUpcomingContents(
        @Path(value = "course_id", encoded = true) courseId: Long,
        @QueryMap queryParams: HashMap<String, Any>
    ): ApiResponse<List<ContentEntityLite>>

    @GET("$CONTENTS_PATH_v2_4{content_id}/")
    fun getNetworkContentWithId(
        @Path(value = "content_id", encoded = true) contentId: Long
    ): RetrofitCall<NetworkContent>

    @GET("${TestpressExamApiClient.EXAMS_LIST_v2_3_PATH}{exam_slug}${TestpressExamApiClient.LANGUAGES_PATH}")
    fun getLanguages(
        @Path(value = "exam_slug", encoded = true) examSlug: String?
    ): RetrofitCall<TestpressApiResponse<NetworkLanguage>>

    @GET("api/v2.4/exams/{exam_id}/questions/")
    fun getQuestions(
        @Path(value = "exam_id", encoded = true) examId: Long,
        @QueryMap queryParams: HashMap<String, Any>
    ): RetrofitCall<ApiResponse<NetworkOfflineQuestionResponse>>

    @GET("api/v3/exams/")
    fun getExams(
        @QueryMap queryParams: HashMap<String, Any>
    ): RetrofitCall<ApiResponse<List<NetworkExamContent>>>

    @POST("/api/v3/exams/{exam_id}/submit-offline-exam-answers/")
    fun updateOfflineAnswers(
        @Path(value = "exam_id", encoded = true) examId: Long,
        @Body arguments: HashMap<String, Any>
    ): RetrofitCall<HashMap<String,String>>
}


class CourseNetwork(context: Context) : TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {
    private fun getCourseService() = retrofit.create(CourseService::class.java)

    fun getNetworkContent(url: String): RetrofitCall<NetworkContent> {
        return getCourseService().getNetworkContent(url)
    }

    fun createContentAttempt(
        contentId: Long,
        queryParams: HashMap<String, Int> = hashMapOf()
    ): RetrofitCall<NetworkContentAttempt> {
        return getCourseService().createContentAttempt(contentId, queryParams)
    }

    fun getContentAttempts(url: String): RetrofitCall<TestpressApiResponse<NetworkContentAttempt>> {
        return getCourseService().getContentAttempts(url)
    }

    fun endAttempt(url: String):  RetrofitCall<NetworkAttempt> {
        return getCourseService().endAttempt(url)
    }

    fun endContentAttempt(url: String):  RetrofitCall<NetworkContentAttempt> {
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

    fun getProductsCategories(arguments: HashMap<String, Any>): RetrofitCall<ApiResponse<List<ProductCategoryEntity>>> {
        return getCourseService().getProductsCategories(arguments)
    }

    suspend fun getRunningContents(courseId: Long, arguments: HashMap<String, Any>): ApiResponse<List<ContentEntityLite>> {
        return getCourseService().getRunningContents(courseId, arguments)
    }

    suspend fun getUpcomingContents(courseId: Long, arguments: HashMap<String, Any>): ApiResponse<List<ContentEntityLite>> {
        return getCourseService().getUpcomingContents(courseId, arguments)
    }

    fun getNetworkContentWithId(contentId: Long): RetrofitCall<NetworkContent> {
        return getCourseService().getNetworkContentWithId(contentId)
    }

    fun getLanguages(slug: String): RetrofitCall<TestpressApiResponse<NetworkLanguage>> {
        return getCourseService().getLanguages(slug)
    }

    fun getQuestions(
        examId: Long,
        queryParams: HashMap<String, Any>
    ): RetrofitCall<ApiResponse<NetworkOfflineQuestionResponse>> {
        return getCourseService().getQuestions(examId, queryParams)
    }

    fun getExams(
        queryParams: HashMap<String, Any>
    ): RetrofitCall<ApiResponse<List<NetworkExamContent>>> {
        return getCourseService().getExams(queryParams)
    }

    fun updateOfflineAnswers(
        examId: Long,
        offlineAttempt: OfflineAttemptDetail,
        offlineAnswers: List<OfflineAnswer>
    ): RetrofitCall<HashMap<String,String>> {
        val body = hashMapOf(
            "offline_attempt" to offlineAttempt,
            "offline_answers" to offlineAnswers
        )
        return getCourseService().updateOfflineAnswers(examId, body)
    }
}