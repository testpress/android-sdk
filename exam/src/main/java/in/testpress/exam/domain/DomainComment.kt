package `in`.testpress.exam.domain

import `in`.testpress.database.entities.CommentEntity
import `in`.testpress.database.entities.ContentObject
import `in`.testpress.database.entities.ProfileDetails

data class DomainComment(
    var id: Long? = null,
    var contentObject: DomainContentObject? = null,
    var user: DomainProfileDetails? = null,
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

data class DomainContentObject (
    var id: Long? = null,
    var url: String? = null
)

data class DomainProfileDetails (
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

fun List<CommentEntity>.asDomainComment(): List<DomainComment> {
    return this.map {
        createDomainContent(it)
    }
}

private fun createDomainContent(comment: CommentEntity): DomainComment {
    return DomainComment(
         id = comment.id,
         contentObject = comment.contentObject?.asDomainContent(),
         user = comment.user?.asDomainProfileDetail(),
         url = comment.url,
         userUrl = comment.userUrl,
         userEmail = comment.userEmail,
         comment = comment.comment,
         submitDate = comment.submitDate,
         upvotes = comment.upvotes,
         downvotes = comment.downvotes,
         typeOfVote = comment.typeOfVote,
         voteId = comment.voteId
    )
}

fun ContentObject.asDomainContent(): DomainContentObject {
    return DomainContentObject(
            id = this.id,
            url = this.url
    )
}

fun ProfileDetails.asDomainProfileDetail(): DomainProfileDetails {
    return DomainProfileDetails(
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
