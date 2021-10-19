package `in`.testpress.models

import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.database.entities.DiscussionThreadAnswerEntity
import `in`.testpress.database.entities.UserEntity

data class NetworkDiscussionThreadAnswer (
    var id: Long? = null,
    var forumThreadId: Long? = null,
    val approvedBy: UserEntity? = null,
    val comment: CommentEntity? = null
)

fun NetworkDiscussionThreadAnswer.asDatabaseModel(): DiscussionThreadAnswerEntity {
    return DiscussionThreadAnswerEntity(
        id!!,
        forumThreadId,
        approvedBy,
        comment
    )
}