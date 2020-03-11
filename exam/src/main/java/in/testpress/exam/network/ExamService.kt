package `in`.testpress.exam.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.exam.api.TestpressExamApiClient.EXAMS_LIST_v2_3_PATH
import `in`.testpress.exam.api.TestpressExamApiClient.LANGUAGES_PATH
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.greendao.Language
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import android.content.Context
import retrofit2.http.GET
import retrofit2.http.Path

interface ExamService {
    @GET("$EXAMS_LIST_v2_3_PATH{exam_slug}$LANGUAGES_PATH")
    fun getLanguages(
        @Path(value = "exam_slug", encoded = true) examSlug: String?
    ): RetrofitCall<TestpressApiResponse<NetworkLanguage>>
}

class ExamNetwork(context: Context): TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {
    private fun getService() = retrofit.create(ExamService::class.java)

    fun getLanguages(slug: String): RetrofitCall<TestpressApiResponse<NetworkLanguage>> {
        return getService().getLanguages(slug)
    }
}
