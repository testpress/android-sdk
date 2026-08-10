package `in`.testpress.exam.network.service

import `in`.testpress.core.TestpressSdk
import `in`.testpress.exam.api.TestpressExamApiClient.*
import `in`.testpress.exam.models.Vote
import `in`.testpress.exam.network.NetworkComment
import `in`.testpress.exam.network.NetworkCommentResponse
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import android.content.Context
import retrofit2.http.*
import java.util.*

@JvmSuppressWildcards
interface CommentService {

    @GET
    fun getComments(@Url commentsUrl: String): RetrofitCall<NetworkCommentResponse>

    @POST
    fun postComment(@Url commentsUrl: String?,
                    @Body arguments: HashMap<String, String>): RetrofitCall<NetworkComment>

    @POST(VOTES_PATH)
    fun voteComment(@Body params: HashMap<String, Any>): RetrofitCall<Vote<NetworkComment>>

    @DELETE("$VOTES_PATH{vote_id}/")
    fun deleteCommentVote(@Path(value = "vote_id") id: Long?): RetrofitCall<String>

    @PUT("$VOTES_PATH{vote_id}/")
    fun updateCommentVote(
            @Path(value = "vote_id") id: Long?,
            @Body params: HashMap<String, Any>): RetrofitCall<Vote<NetworkComment>>
}

open class CommentApiClient(context: Context): TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {
    private fun getService() = retrofit.create(CommentService::class.java)

    fun getComments(urlFrag: String): RetrofitCall<NetworkCommentResponse> {
        return getService().getComments(urlFrag)
    }

    fun postComment(urlFrag: String?, comment: String): RetrofitCall<NetworkComment> {
        val params = HashMap<String, String>()
        params["comment"] = comment
        return getService().postComment(urlFrag, params)
    }

    fun voteComment(comment: NetworkComment, typeOfVote: Int): RetrofitCall<Vote<NetworkComment>> {
        val params = HashMap<String, Any>()
        params["content_object"] = comment
        params["type_of_vote"] = typeOfVote
        return getService().voteComment(params)
    }

    fun deleteCommentVote(comment: NetworkComment): RetrofitCall<String> {
        return getService().deleteCommentVote(comment.voteId)
    }

    fun updateCommentVote(comment: NetworkComment, typeOfVote: Int): RetrofitCall<Vote<NetworkComment>> {
        val params = HashMap<String, Any>()
        params["content_object"] = comment
        params["type_of_vote"] = typeOfVote
        return getService().updateCommentVote(comment.voteId, params)
    }
}
