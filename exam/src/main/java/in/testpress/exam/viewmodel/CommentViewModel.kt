package `in`.testpress.exam.viewmodel

import `in`.testpress.exam.domain.DomainComment
import `in`.testpress.exam.network.NetworkComment
import `in`.testpress.exam.repository.CommentRepository
import `in`.testpress.network.Resource
import `in`.testpress.network.TestpressApiClient
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class CommentViewModel @ViewModelInject constructor(
        private val repository: CommentRepository,
        @Assisted private val savedStateHandle: SavedStateHandle
): ViewModel() {

    var postCommentResponse = repository.postCommentResponse

    var uploadImageResponse = repository.uploadImageResponse

    var voteCommentVoteResponse = repository.voteCommentResponse

    var updateCommentVoteResponse = repository.updateCommentVoteResponse

    var deleteCommentVoteResponse = repository.deleteCommentVoteResponse

    fun getComments(commentsUrl: String): LiveData<Resource<List<DomainComment>>> {
        return repository.getComments(urlFrag = commentsUrl)
    }

    fun postComment(commentsUrl: String, comment: String) {
        return repository.postComment(commentsUrl, comment)
    }

    fun voteComment(comment: NetworkComment, typeOfVote: Int) {
        return repository.voteComment(comment, typeOfVote)
    }

    fun updateCommentVote(comment: NetworkComment, typeOfVote: Int) {
        return repository.updateCommentVote(comment, typeOfVote)
    }

    fun deleteCommentVote(comment: NetworkComment) {
        return repository.deleteCommentVote(comment)
    }

    fun uploadImage(testPressApiClient: TestpressApiClient, imagePath: String) {
        return repository.uploadImage(testPressApiClient,imagePath)
    }
}
