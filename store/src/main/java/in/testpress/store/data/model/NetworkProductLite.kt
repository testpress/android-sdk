package `in`.testpress.store.data.model

import `in`.testpress.database.entities.ProductLiteEntity


data class NetworkProductLite(
    val id: Int?,
    val title: String?,
    val slug: String?,
    val images: List<NetworkImage>? = null,
    val categoryId: Int?,
    val contentsCount: Int = 0,
    val chaptersCount: Int = 0,
    val order: Int?,
    val price: String?
)

fun NetworkProductLite.asDomain(): ProductLiteEntity {
    return ProductLiteEntity(
        this.id ?: -1,
        this.title ?: "",
        this.slug ?: "",
        this.images?.asDomain(),
        this.categoryId,
        this.contentsCount,
        this.chaptersCount,
        this.order ?: -1,
        this.price ?: ""
    )
}

fun List<NetworkProductLite>.asDomain(): List<ProductLiteEntity> = this.map { it.asDomain() }