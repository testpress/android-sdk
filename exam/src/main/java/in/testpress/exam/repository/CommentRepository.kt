package `in`.testpress.exam.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.database.dao.CommentDao
import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.exam.domain.DomainComment
import `in`.testpress.exam.domain.asDomainComment
import `in`.testpress.exam.models.Vote
import `in`.testpress.exam.network.NetworkComment
import `in`.testpress.exam.network.NetworkCommentResponse
import `in`.testpress.exam.network.asDatabaseModel
import `in`.testpress.exam.network.service.CommentApiClient
import `in`.testpress.models.FileDetails
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.util.WebViewUtils
import android.os.AsyncTask
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import javax.inject.Inject

open class CommentRepository @Inject constructor(
        private val commentDao: CommentDao,
        val commentApiClient: CommentApiClient
) {

    var postCommentResponse = MutableLiveData<Resource<NetworkComment>>()

    var voteCommentResponse = MutableLiveData<Resource<Vote<NetworkComment>>>()

    var updateCommentVoteResponse = MutableLiveData<Resource<Vote<NetworkComment>>>()

    var deleteCommentVoteResponse = MutableLiveData<Resource<String>>()

    var uploadImageResponse = MutableLiveData<Resource<String>>()

    fun getComments(
            forceRefresh: Boolean = true,
            urlFrag: String
    ): LiveData<Resource<List<DomainComment>>> {
        return object : NetworkBoundResource<List<DomainComment>, NetworkCommentResponse>() {
            override fun saveNetworkResponseToDB(item: NetworkCommentResponse) {
                item.results?.let { saveCommentToDb(it) }
            }

            override fun shouldFetch(data: List<DomainComment>?): Boolean {
                return forceRefresh || data == null
            }

            override fun loadFromDb(): LiveData<List<DomainComment>> {
                val liveData = MutableLiveData<List<DomainComment>>()
                liveData.postValue(getCommentFromDB()?.asDomainComment())
                return liveData
            }

            override fun createCall(): RetrofitCall<NetworkCommentResponse> {
                return commentApiClient.getComments(urlFrag)
            }
        }.asLiveData()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun saveCommentToDb(item: List<NetworkComment>) {
        item.forEach {
            commentDao.insert(it.asDatabaseModel())
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getCommentFromDB(): List<CommentEntity>? = runBlocking {
        val job = CoroutineScope(Dispatchers.IO).async {
            commentDao.getAll()
        }
        return@runBlocking job.await()
    }

    fun postComment(commentsUrl: String, comment: String) {
        commentApiClient.postComment(commentsUrl, comment)
                .enqueue(object : TestpressCallback<NetworkComment>() {
                    override fun onSuccess(result: NetworkComment) {
                        postCommentResponse.value = Resource.success(result)
                    }

                    override fun onException(exception: TestpressException) {
                        postCommentResponse.value = Resource.error(exception, null)
                    }
                })
    }

    fun voteComment(comment: NetworkComment, typeOfVote: Int) {
        commentApiClient.voteComment(comment, typeOfVote)
                .enqueue(object : TestpressCallback<Vote<NetworkComment>>() {
                    override fun onSuccess(result: Vote<NetworkComment>?) {
                        voteCommentResponse.value = Resource.success(result)
                    }

                    override fun onException(exception: TestpressException) {
                        voteCommentResponse.value = Resource.error(exception, null)
                    }

                })
    }

    fun updateCommentVote(comment: NetworkComment, typeOfVote: Int) {
        commentApiClient.updateCommentVote(comment, typeOfVote)
                .enqueue(object : TestpressCallback<Vote<NetworkComment>>() {
                    override fun onSuccess(result: Vote<NetworkComment>) {
                        updateCommentVoteResponse.value = Resource.success(result)
                    }

                    override fun onException(exception: TestpressException) {
                        updateCommentVoteResponse.value = Resource.error(exception, null)
                    }

                })
    }

    fun deleteCommentVote(comment: NetworkComment) {
        commentApiClient.deleteCommentVote(comment)
                .enqueue(object : TestpressCallback<String>() {
                    override fun onSuccess(response: String) {
                        deleteCommentVoteResponse.value = Resource.success(response)
                    }

                    override fun onException(exception: TestpressException) {
                        deleteCommentVoteResponse.value = Resource.error(exception, null)
                    }
                })
    }

    fun uploadImage(testPressApiClient: TestpressApiClient, imagePath: String) {
        testPressApiClient.upload(imagePath)
                .enqueue(object : TestpressCallback<FileDetails>() {
                    override fun onSuccess(fileDetails: FileDetails) {
                        uploadImageResponse.value = Resource.success(WebViewUtils.appendImageTags(fileDetails.url))
                    }

                    override fun onException(exception: TestpressException) {
                        uploadImageResponse.value = Resource.error(exception, null)
                    }
                })
    }
}
