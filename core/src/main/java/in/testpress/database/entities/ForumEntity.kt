package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class ForumEntity(
    @PrimaryKey
    var id: Long? = null,
    val shortWebUrl: String? = null,
    val shortUrl: String? = null,
    val webUrl: String? = null,
    val created: String? = null,
    val commentsUrl: String? = null,
    val url: String? = null,
    val modified: String? = null,
    val upvotes: Int? = null,
    val downvotes: Int? = null,
    val title: String? = null,
    val summary: String? = null,
    val isActive: Boolean? = null,
    val publishedDate: String? = null,
    val commentsCount: Int? = null,
    val isLocked: Boolean? = null,
    val subject: Int? = null,
    val viewsCount: Int? = null,
    val participantsCount: Int? = null,
    val lastCommentedTime: String? = null,
    val contentHtml: String? = null,
    val isPublic: Boolean? = null,
    val shortLink: String? = null,
    val institute: Int? = null,
    val slug: String? = null,
    val isPublished: Boolean? = null,
    val isApproved: Boolean? = null,
    val forum: Boolean? = null,
    val ipAddress: String? = null,
    val voteId: Long? = null,
    val typeOfVote: Int? = null,
    val published: Long? = null,
    val modifiedDate: Long? = null,
    val creatorId: Long? = null,
    val commentorId: Long? = null,
    val categoryId: Long? = null
)