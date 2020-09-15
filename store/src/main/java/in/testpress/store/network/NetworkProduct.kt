package `in`.testpress.store.network

import `in`.testpress.database.CourseEntity
import `in`.testpress.database.ProductEntity

data class NetworkProductResponse(
    var next: String? = null,
    var perPage: Int? = null,
    var previous: String? = null,
    var count: Int? = null,
    var results: Results? = null
)

data class Results(
    var courses: List<NetworkCourse?>? = null,
    var prices: List<NetworkPrice?>? = null,
    var products: List<NetworkProduct?>? = null
)

data class NetworkProduct(
    var endDate: String? = null,
    var image: String? = null,
    var courses: List<Int?>? = null,
    var surl: String? = null,
    var title: String? = null,
    var paymentLink: String? = null,
    var buyNowText: String? = null,
    var furl: String? = null,
    var id: Int? = null,
    var descriptionHtml: String? = null,
    var currentPrice: String? = null,
    var prices: List<Int?>? = null,
    var slug: String? = null,
    var startDate: String? = null
)

data class NetworkCourse(
    var image: String? = null,
    var examsCount: Int? = null,
    var created: String? = null,
    var description: String? = null,
    var title: String? = null,
    var chaptersCount: Int? = null,
    var deviceAccessControl: String? = null,
    var createdBy: Int? = null,
    var enableDiscussions: Boolean? = null,
    var url: String? = null,
    var contentsCount: Int? = null,
    var contentsUrl: String? = null,
    var chaptersUrl: String? = null,
    var modified: String? = null,
    var videosCount: Int? = null,
    var externalContentLink: String? = null,
    var id: Int? = null,
    var attachmentsCount: Int? = null,
    var slug: String? = null,
    var htmlContentsCount: Int? = null,
    var order: Int? = null,
    var externalLinkLabel: String? = null
)

data class NetworkPrice(
    var id: Int? = null,
    var name: String? = null,
    var price: String? = null,
    var validity: Int? = null,
    var endDate: String? = null,
    var startDate: String? = null
)

data class ProductCourse (
    var courseId: Int,
    var productId: Int
)

fun NetworkProduct.asDatabaseModel(): ProductEntity {
    return ProductEntity(
            id = this.id,
            endDate = this.endDate,
            image = this.image,
            surl = this.surl,
            title = this.title,
            paymentLink = this.paymentLink,
            buyNowText = this.buyNowText,
            furl = this.furl,
            descriptionHtml = this.descriptionHtml,
            currentPrice = this.currentPrice,
            slug = this.slug,
            startDate = this.startDate
    )
}

fun NetworkCourse.asDatabaseModel(): CourseEntity {
    return CourseEntity(
            image = this.image,
            examsCount = this.examsCount,
            created = this.created,
            description = this.description,
            title = this.title,
            chaptersCount = this.chaptersCount,
            deviceAccessControl = this.deviceAccessControl,
            createdBy= this.createdBy,
            enableDiscussions = this.enableDiscussions,
            url = this.url,
            contentsCount = this.contentsCount,
            contentsUrl = this.contentsUrl,
            chaptersUrl = this.chaptersUrl,
            modified = this.modified,
            videosCount = this.videosCount,
            externalContentLink = this.externalContentLink,
            id = this.id
    )
}


