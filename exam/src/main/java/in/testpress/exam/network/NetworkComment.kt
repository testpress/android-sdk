package `in`.testpress.exam.network

import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.database.entities.ContentObject
import `in`.testpress.database.entities.ProfileDetails
import `in`.testpress.exam.domain.DomainComment
import `in`.testpress.exam.domain.DomainContentObject
import `in`.testpress.exam.domain.DomainProfileDetails

data class NetworkCommentResponse(
   var results: List<NetworkComment>? = null
)

data class NetworkComment(
    var id: Long? = null,
    var contentObject: NetworkContentObject? = null,
    var user: NetworkProfileDetails? = null,
    var url: String? = null,
    var userEmail: String? = null,
    var userUrl: String?= null,
    var comment: String? = null,
    var submitDate: String? =null,
    var upvotes: Long? = null,
    var downvotes: Long? = null,
    var typeOfVote: Long? = null,
    var voteId: Long? = null
)

data class NetworkContentObject (
    var id: Long? = null,
    var url: String? = null
)

data class NetworkProfileDetails (
    var id: Long? = null,
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

fun NetworkComment.asDatabaseModel(): CommentEntity {
    return CommentEntity(
        id = this.id,
        contentObject = this.contentObject?.asDatabaseContent(),
        user = this.user?.asDatabaseProfileDetail(),
        url = this.url,
        userEmail = this.userEmail,
        userUrl = this.userUrl,
        comment = this.comment,
        submitDate = this.submitDate,
        upvotes = this.upvotes,
        downvotes = this.downvotes,
        typeOfVote = this.typeOfVote,
        voteId = this.voteId
    )
}

fun NetworkContentObject.asDatabaseContent(): ContentObject {
    return ContentObject(
            id = this.id,
            url = this.url
    )
}

fun NetworkProfileDetails.asDatabaseProfileDetail(): ProfileDetails {
    return ProfileDetails(
            id = this.id,
            url = this.url,
            username = this.username,
            displayName = this.displayName,
            firstName = this.firstName,
            lastName = this.lastName,
            email = this.email,
            photo = this.photo,
            largeImage = this.largeImage,
            mediumImage = this.mediumImage,
            smallImage = this.smallImage,
            xSmallImage = this.xSmallImage,
            miniImage = this.miniImage,
            birthDate = this.birthDate,
            gender = this.gender,
            address1 = this.address1,
            address2 = this.address2,
            city = this.city,
            zip = this.zip,
            state = this.state,
            stateChoices = this.stateChoices,
            phone = this.phone
    )
}

fun DomainComment.asNetworkComment(): NetworkComment {
    return NetworkComment(
            id = this.id,
            contentObject = this.contentObject?.asNetworkContentObject(),
            user = this.user?.asNetworkProfileDetail(),
            url = this.url,
            userEmail = this.userEmail,
            userUrl = this.userUrl,
            comment = this.comment,
            submitDate = this.submitDate,
            upvotes = this.upvotes,
            downvotes = this.downvotes,
            typeOfVote = this.typeOfVote,
            voteId = this.voteId
    )
}

fun DomainContentObject.asNetworkContentObject(): NetworkContentObject {
    return NetworkContentObject(
            id = this.id,
            url = this.url
    )
}

fun DomainProfileDetails.asNetworkProfileDetail(): NetworkProfileDetails {
    return NetworkProfileDetails(
            id = this.id,
            url = this.url,
            username = this.username,
            displayName = this.displayName,
            firstName = this.firstName,
            lastName = this.lastName,
            email = this.email,
            photo = this.photo,
            largeImage = this.largeImage,
            mediumImage = this.mediumImage,
            smallImage = this.smallImage,
            xSmallImage = this.xSmallImage,
            miniImage = this.miniImage,
            birthDate = this.birthDate,
            gender = this.gender,
            address1 = this.address1,
            address2 = this.address2,
            city = this.city,
            zip = this.zip,
            state = this.state,
            stateChoices = this.stateChoices,
            phone = this.phone
    )
}
