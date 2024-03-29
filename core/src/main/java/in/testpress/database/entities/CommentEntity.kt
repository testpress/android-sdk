package `in`.testpress.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CommentEntity(
    @PrimaryKey
    var id: Long? = null,
    @Embedded
    var contentObject: ContentObject? = null,
    @Embedded
    var user: ProfileDetails? = null,
    var url: String? = null,
    var userEmail: String? = null,
    var userUrl: String?= null,
    var comment: String? = null,
    var submitDate: String? =null,
    var upvotes: Long? = null,
    var downvotes: Long? = null,
    var typeOfVote: Long? = null,
    var voteId: Long? = null,
    var created: String? =null,
    var modified: String? =null
)

data class ContentObject (
    @ColumnInfo(name = "contentId")
    var id: Long? = null,
    @ColumnInfo(name = "contentUrl")
    var url: String? = null
)

data class ProfileDetails (
    @ColumnInfo(name = "profileId")
    var id: Long? = null,
    @ColumnInfo(name = "profileUrl")
    var url: String? = null,
    var username: String? = null,
    var displayName: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var photo: String? = null,
    var largeImage: String? = null,
    var mediumImage: String? = null,
    var smallImage: String? = null,
    var xSmallImage: String? = null,
    var miniImage: String? = null,
    var birthDate: String? = null,
    var gender: String? = null,
    var address1: String? = null,
    var address2: String? = null,
    var city: String? = null,
    var zip: String? = null,
    var state: String? = null,
    var stateChoices: String? = null,
    var phone: String? = null
)
