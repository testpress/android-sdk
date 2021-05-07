package `in`.testpress.network

import `in`.testpress.database.CourseEntity
import `in`.testpress.database.PriceEntity
import `in`.testpress.database.ProductEntity

data class NetworkProductResponse(
    var courses: List<NetworkCourse?>? = null,
    var prices: List<NetworkPrice?>? = null,
    var products: List<NetworkProduct?>? = null
)

data class NetworkProduct(
    val endDate: String? = null,
    val image: String? = null,
    val courses: List<Long>? = null,
    val surl: String? = null,
    val title: String? = null,
    val paymentLink: String? = null,
    val buyNowText: String? = null,
    val furl: String? = null,
    val id: Long,
    val descriptionHtml: String? = null,
    val currentPrice: String? = null,
    val prices: List<Long>? = null,
    val slug: String? = null,
    val startDate: String? = null
)

data class NetworkCourse(
    val image: String? = null,
    val examsCount: Int? = null,
    val created: String? = null,
    val description: String? = null,
    val title: String? = null,
    val chaptersCount: Int? = null,
    val deviceAccessControl: String? = null,
    val createdBy: Int? = null,
    val enableDiscussions: Boolean? = null,
    val url: String? = null,
    val contentsCount: Int? = null,
    val contentsUrl: String? = null,
    val chaptersUrl: String? = null,
    val modified: String? = null,
    val videosCount: Int? = null,
    val externalContentLink: String? = null,
    val id: Long? = null,
    val attachmentsCount: Int? = null,
    val slug: String? = null,
    val htmlContentsCount: Int? = null,
    val order: Int? = null,
    val externalLinkLabel: String? = null
)


fun NetworkCourse.asDatabaseModel(): CourseEntity {
    return CourseEntity(
        image = this.image,
        examsCount = this.examsCount,
        created = this.created,
        description = this.description,
        title = this.title,
        chaptersCount = this.chaptersCount,
        deviceAccessControl = this.deviceAccessControl,
        createdBy = this.createdBy,
        enableDiscussions = this.enableDiscussions,
        url = this.url,
        contentsCount = this.contentsCount,
        contentsUrl = this.contentsUrl,
        chaptersUrl = this.chaptersUrl,
        modified = this.modified,
        videosCount = this.videosCount,
        externalContentLink = this.externalContentLink,
        id = this.id,
        attachmentsCount = this.attachmentsCount,
        slug = this.slug,
        htmlContentsCount = this.htmlContentsCount,
        order = this.order,
        externalLinkLabel = this.externalLinkLabel
    )
}

fun NetworkProduct.asDatabaseModel(): ProductEntity {
    return ProductEntity(
        endDate         = this.endDate,
        image           = this.image,
        surl            = this.surl,
        title           = this.title,
        paymentLink     = this.paymentLink,
        buyNowText      = this.buyNowText,
        furl            = this.furl,
        id              = this.id,
        descriptionHtml = this.descriptionHtml,
        currentPrice    = this.currentPrice,
        slug            = this.slug,
        startDate       = this.startDate
    )
}


data class NetworkPrice(
    var id: Int? = null,
    var name: String? = null,
    var price: String? = null,
    var validity: Int? = null,
    var endDate: String? = null,
    var startDate: String? = null
)


fun NetworkPrice.asDatabaseModel(): PriceEntity {
    return PriceEntity(
        id        = this.id,
        name      = this.name,
        price     = this.price,
        validity  = this.validity,
        endDate   = this.endDate,
        startDate = this.startDate
    )
}
