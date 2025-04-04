package `in`.testpress.store.data.model

import `in`.testpress.database.entities.ProductCategoryEntity

data class NetworkProductCategory(
    val id: Int?,
    val name: String?,
    val slug: String?
)

fun NetworkProductCategory.asDomain(): ProductCategoryEntity {
    return ProductCategoryEntity(this.id, this.name, this.slug)
}

fun List<NetworkProductCategory>.asDomain(): List<ProductCategoryEntity> = this.map { it.asDomain() }