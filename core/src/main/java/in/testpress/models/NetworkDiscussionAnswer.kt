package `in`.testpress.models

import `in`.testpress.database.ContentEntity
import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.database.entities.DiscussionAnswerEntity
import `in`.testpress.database.entities.UserEntity

data class NetworkDiscussionAnswer (
    var id: Long? = null,
    var forumThreadId: Long? = null,
    val approvedBy: UserEntity? = null,
    val comment: CommentEntity? = null
)

fun NetworkDiscussionAnswer.asDatabaseModel(): DiscussionAnswerEntity {
    return DiscussionAnswerEntity(
        id!!,
        forumThreadId,
        approvedBy,
        comment
    )
}