package `in`.testpress.store.data.model.mapping

import `in`.testpress.database.entities.Image
import `in`.testpress.database.entities.PriceEntity
import `in`.testpress.database.entities.ProductWithPrices
import `in`.testpress.store.models.Images
import `in`.testpress.store.models.PricesItem
import `in`.testpress.store.models.Product

fun ProductWithPrices.asProduct(): Product {
    val product = Product()
    product.id = this.product.id
    product.url = this.product.url
    product.title = this.product.title
    product.slug = this.product.slug
    product.image = null
    product.startDate = this.product.startDate
    product.endDate = this.product.endDate
    product.categories = arrayListOf()
    product.types = arrayListOf()
    product.examsCount = this.product.examsCount
    product.notesCount = this.product.htmlCount
    product.price = this.product.price
    product.images = this.product.images?.map { it.asDomainImage() }
    product.buyNowText = this.product.buyNowText
    product.description = this.product.description
    product.additionalInfo = null
    product.paymentLink = this.product.paymentLink
    product.institute = this.product.institute
    product.requiresShipping = this.product.requiresShipping
    product.exams = arrayListOf()
    product.notes = arrayListOf()
    product.prices = this.prices.map { it.asDomainPricesItem() }

    return product
}

fun Image.asDomainImage(): Images {
    val images = Images()
    images.small = this.small
    images.medium = this.medium
    images.original = this.original
    return images
}

fun PriceEntity.asDomainPricesItem(): PricesItem {
    return PricesItem(
        id = id,
        name = name,
        price = price,
        validity = validity?.toInt(),
        endDate = endDate,
        startDate = startDate
    )
}