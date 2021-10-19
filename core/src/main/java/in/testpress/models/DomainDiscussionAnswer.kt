package `in`.testpress.models

import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.database.entities.DiscussionAnswerEntity
import `in`.testpress.database.entities.UserEntity

data class DomainDiscussionAnswer (
    var id: Long? = null,
    var forumThreadId: Long? = null,
    val approvedBy: UserEntity? = null,
    val comment: CommentEntity? = null
)


fun DiscussionAnswerEntity.asDomainModel(): DomainDiscussionAnswer {
    return DomainDiscussionAnswer(
        id, forumThreadId, approvedBy, comment
    )
}