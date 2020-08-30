package `in`.testpress.store.models

import `in`.testpress.database.CoursesItem
import `in`.testpress.database.ProductsItem
import `in`.testpress.database.ProductsListEntity

data class ProductsList(
		val courses: List<CoursesItem>? = null,
		val prices: List<PricesItem>? = null,
		val products: List<ProductsItem>? = null
)

fun ProductsList.asDatabaseModel(): ProductsListEntity {
	return ProductsListEntity(
		courses = this.courses,
		products = this.products
	)
}
