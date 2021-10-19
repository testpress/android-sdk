package `in`.testpress.models

import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.database.entities.DiscussionThreadAnswerEntity
import `in`.testpress.database.entities.UserEntity

data class DomainDiscussionThreadAnswer (
    var id: Long? = null,
    var forumThreadId: Long? = null,
    val approvedBy: UserEntity? = null,
    val comment: CommentEntity? = null
)


fun DiscussionThreadAnswerEntity.asDomainModel(): DomainDiscussionThreadAnswer {
    return DomainDiscussionThreadAnswer(
        id, forumThreadId, approvedBy, comment
    )
}