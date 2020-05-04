package `in`.testpress.exam.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.exam.api.TestpressExamApiClient.EXAMS_LIST_v2_3_PATH
import `in`.testpress.exam.api.TestpressExamApiClient.LANGUAGES_PATH
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap
import java.util.HashMap

@JvmSuppressWildcards
interface ExamService {
    @GET("$EXAMS_LIST_v2_3_PATH{exam_slug}$LANGUAGES_PATH")
    fun getLanguages(
        @Path(value = "exam_slug", encoded = true) examSlug: String?
    ): RetrofitCall<TestpressApiResponse<NetworkLanguage>>

    @GET("{questions_url}")
    fun getQuestions(
        @Path(value="questions_url", encoded = true) questionsUrl: String?
    ): RetrofitCall<ApiResponse<NetworkExamQuestionResult>>

    @GET("{questions_url}")
    fun getUserSelectedAnswers(
        @Path(value="questions_url", encoded = true) questionsUrl: String?,
        @QueryMap options: Map<String, Any>
    ): RetrofitCall<TestpressApiResponse<NetworkUserSelectedAnswer>>

    @PUT("{url}")
    fun saveUserSelectedAnswer(
        @Path(value="url", encoded = true) url: String?,
        @Body arguments: HashMap<String, Any>
    ): RetrofitCall<NetworkUserSelectedAnswer>
}

class ExamNetwork(context: Context): TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {
    private fun getService() = retrofit.create(ExamService::class.java)

    fun getLanguages(slug: String): RetrofitCall<TestpressApiResponse<NetworkLanguage>> {
        return getService().getLanguages(slug)
    }

    fun getQuestions(url: String): RetrofitCall<ApiResponse<NetworkExamQuestionResult>> {
        return getService().getQuestions(url)
    }

    fun getUserSelectedAnswers(url: String, queryParams: Map<String, Any>): RetrofitCall<TestpressApiResponse<NetworkUserSelectedAnswer>> {
        return getService().getUserSelectedAnswers(url, queryParams)
    }

    fun saveUserSelectedAnswer(url: String, arguments: HashMap<String, Any>): RetrofitCall<NetworkUserSelectedAnswer> {
        return getService().saveUserSelectedAnswer(url, arguments)
    }
}
