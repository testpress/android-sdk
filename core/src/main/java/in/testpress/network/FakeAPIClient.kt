package `in`.testpress.network

import `in`.testpress.models.NetworkCategory
import `in`.testpress.models.NetworkForum
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.models.User
import `in`.testpress.models.greendao.Content
import android.content.Context
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Response

class FakeAPIClient(context: Context, val nextPage: Boolean=false) : APIClient(context) {
    var failureMessage: String? = null

    override suspend fun getDiscussions(queryParams: Map<String, Any>): Response<TestpressApiResponse<NetworkForum>> {
        if (failureMessage != null) {
            return buildErrorResponse(400)
        }
        val response = TestpressApiResponse<NetworkForum>()
        response.results = listOf(getNetworkForum())
        response.next = if (nextPage) "http://localhost:800" else null
        return Response.success(response)
    }

    private fun buildErrorResponse(errorCode: Int): Response<TestpressApiResponse<NetworkForum>> {
        val responseBody = ResponseBody.create(MediaType.parse("application/json"), """{"detail":"Error"}""")
        return Response.error<TestpressApiResponse<NetworkForum>>(responseBody, okhttp3.Response.Builder()
                .code(errorCode)
                .message("Error")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build())
    }

    fun getNetworkForum(): NetworkForum {
        return NetworkForum(
                id = 6550,
                shortWebUrl = "https://brilliantpalalms.testpress.in/p/43p6c6/",
                shortUrl = "https://brilliantpalalms.testpress.in/api/v2.3/forum/43p6c6/",
                webUrl = "https://brilliantpalalms.testpress.in/posts/help-help-help/",
                created = "2021-06-23T16:25:50.473836Z",
                commentsUrl = "https://brilliantpalalms.testpress.in/api/v2.3/forum/6550/comments/",
                url = "https://brilliantpalalms.testpress.in/api/v2.3/forum/help-help-help/",
                modified = "2021-06-24T03:30:38.556042Z",
                upvotes = 0, downvotes = 0,
                title = "help help help",
                summary = "thermodynamics chemistry apply cheyyan ulla equations and important theoryum ulla short note aarengilu ayachtharuoo?",
                isActive = true,
                publishedDate = "2021-06-23T16:25:50.473836Z",
                commentsCount = 22, isLocked = false, subject = null,
                viewsCount = 104, participantsCount = 6,
                lastCommentedTime = "2021-06-23T16:46:07.810845Z",
                contentHtml = null, isPublic = null, shortLink = null,
                institute = null, slug = "help-help-help", isPublished = null,
                isApproved = null, forum = null, ipAddress = null, voteId = null,
                typeOfVote = null, published = null, modifiedDate = null, creatorId = null,
                commentorId = null, categoryId = null,
                createdBy = User(
                        id = 21307,
                        url = "https://brilliantpalalms.testpress.in/api/v2.3/users/21307/",
                        username = null, firstName = "appukuttan", lastName = "",
                        displayName = "appukuttan", photo = "https://static.testpress.in/institute/brilliantpalalms/user_profiles/21307/2cbfd9b0fe1b4766b0068c0a56eb8ecd.jpg",
                        largeImage = "https://media.testpress.in/institute/brilliantpalalms/user_profiles/21307/c3501ed886694fad97243486cd8ba544.jpeg",
                        mediumImage = "https://media.testpress.in/institute/brilliantpalalms/user_profiles/21307/b05124ac3e684b2c92225281478928f7.jpeg",
                        mediumSmallImage = "https://media.testpress.in/institute/brilliantpalalms/user_profiles/21307/b77b0de4c1d64991bc61ae5fc4eaa0f1.jpeg"
                ),
                category = NetworkCategory(id = 8, name = "CHEMISTRY", color = "ff0000", slug = "chemistry")
        )
    }
}

