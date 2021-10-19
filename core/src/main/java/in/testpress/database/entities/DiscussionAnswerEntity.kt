package `in`.testpress.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DiscussionAnswerEntity (
    @PrimaryKey
    var id: Long? = null,
    var forumThreadId: Long? = null,
    @Embedded(prefix = "approved_by_")
    val approvedBy: UserEntity? = null,
    @Embedded(prefix = "comment_")
    val comment: CommentEntity? = null
)
