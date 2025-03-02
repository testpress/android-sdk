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

fun List<NetworkCourse>.asCourseDatabaseModel(): List<CourseEntity> {
    return this.map {
        CourseEntity(
                image = it.image,
                examsCount = it.examsCount,
                created = it.created,
                description = it.description,
                title = it.title,
                chaptersCount = it.chaptersCount,
                deviceAccessControl = it.deviceAccessControl,
                createdBy = it.createdBy,
                enableDiscussions = it.enableDiscussions,
                url = it.url,
                contentsCount = it.contentsCount,
                contentsUrl = it.contentsUrl,
                chaptersUrl = it.chaptersUrl,
                modified = it.modified,
                videosCount = it.videosCount,
                externalContentLink = it.externalContentLink,
                id = it.id,
                attachmentsCount = it.attachmentsCount,
                slug = it.slug,
                htmlContentsCount = it.htmlContentsCount,
                order = it.order,
                externalLinkLabel = it.externalLinkLabel
        )
    }
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

fun List<NetworkProduct>.asProductDatabaseModel(): List<ProductEntity> {
    return this.map {
        ProductEntity(
                endDate         = it.endDate,
                image           = it.image,
                surl            = it.surl,
                title           = it.title,
                paymentLink     = it.paymentLink,
                buyNowText      = it.buyNowText,
                furl            = it.furl,
                id              = it.id,
                descriptionHtml = it.descriptionHtml,
                currentPrice    = it.currentPrice,
                slug            = it.slug,
                startDate       = it.startDate
        )
    }
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

fun List<NetworkPrice>.asPriceDatabaseModel(): List<PriceEntity> {
    return this.map {
        PriceEntity(
                id        = it.id,
                name      = it.name,
                price     = it.price,
                validity  = it.validity,
                endDate   = it.endDate,
                startDate = it.startDate
        )
    }
}
