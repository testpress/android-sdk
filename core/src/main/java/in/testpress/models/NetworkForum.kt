package `in`.testpress.models

import `in`.testpress.database.entities.DiscussionPostEntity

data class NetworkForum(
        val shortWebUrl: String? = null,
        val shortUrl: String? = null,
        val webUrl: String? = null,
        val created: String? = null,
        val commentsUrl: String? = null,
        val url: String? = null,
        val id: Long? = null,
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
        val categoryId: Long? = null,
        val createdBy: User? = null,
        val lastCommentedBy: User? = null,
        val category: NetworkCategory? = null
)

fun NetworkForum.asDatabaseModel(): DiscussionPostEntity {
    return DiscussionPostEntity(
            id,
            shortWebUrl,
            shortUrl,
            webUrl,
            created,
            commentsUrl,
            url,
            modified,
            upvotes,
            downvotes,
            title,
            summary,
            isActive,
            publishedDate,
            commentsCount,
            isLocked,
            subject,
            viewsCount,
            participantsCount,
            lastCommentedTime,
            contentHtml,
            isPublic,
            shortLink,
            institute,
            slug,
            isPublished,
            isApproved,
            forum,
            ipAddress,
            voteId,
            typeOfVote,
            published,
            modifiedDate,
            creatorId,
            commentorId,
            categoryId,
            createdBy = createdBy?.asDatabaseModel(),
            lastCommentedBy = lastCommentedBy?.asDatabaseModel(),
            category = category?.asDatabaseModel()
    )
}

fun List<NetworkForum>.asDatabaseModels(): List<DiscussionPostEntity> {
    return this.map {
        it.asDatabaseModel()
    }
}