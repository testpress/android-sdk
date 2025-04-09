package `in`.testpress.store.data.model

import `in`.testpress.database.entities.PriceEntity
import `in`.testpress.database.entities.ProductEntity

data class NetworkProduct(
    val id: Int,
    val url: String? = null,
    val title: String? = null,
    val slug: String? = null,
    val images: List<NetworkImage>? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val description: String? = null,
    val paymentLink: String? = null,
    val descriptionHtml: String? = null,
    val shortDescription: String? = null,
    val order: Int? = null,
    val contentsCount: Int? = null,
    val chaptersCount: Int? = null,
    val videosCount: Int? = null,
    val attachmentsCount: Int? = null,
    val examsCount: Int? = null,
    val quizCount: Int? = null,
    val htmlCount: Int? = null,
    val videoConferenceCount: Int? = null,
    val livestreamCount: Int? = null,
    val price: String? = null,
    val prices: List<NetworkPrice>,
    val strikeThroughPrice: String? = null,
    val institute: String? = null,
    val requiresShipping: Boolean? = null,
    val buyNowText: String? = null,
)

data class NetworkPrice(
    val id: Int,
    val name: String? = null,
    val price: String,
    val validity: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

fun NetworkProduct.toProductEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        url = url,
        title = title,
        slug = slug,
        images = images?.asDomain(),
        startDate = startDate,
        endDate = endDate,
        description = description,
        paymentLink = paymentLink,
        descriptionHtml = descriptionHtml,
        shortDescription = shortDescription,
        contentsCount = contentsCount ?: 0,
        chaptersCount = chaptersCount ?: 0,
        videosCount = videosCount ?: 0,
        attachmentsCount = attachmentsCount ?: 0,
        examsCount = examsCount ?: 0,
        quizCount = quizCount ?: 0,
        htmlCount = htmlCount ?: 0,
        videoConferenceCount = videoConferenceCount ?: 0,
        livestreamCount = livestreamCount ?: 0,
        price = price,
        strikeThroughPrice = strikeThroughPrice,
        institute = institute,
        requiresShipping = requiresShipping,
        buyNowText = buyNowText
    )
}

fun NetworkProduct.toPriceEntities(): List<PriceEntity> {
    return this.prices.map { it.asDomainPrice(this.id) }
}

fun NetworkPrice.asDomainPrice(productId: Int): PriceEntity {
    return PriceEntity(
        id = this.id,
        productId = productId,
        name = this.name,
        price = this.price,
        validity = this.validity,
        startDate = this.startDate,
        endDate = this.endDate
    )
}