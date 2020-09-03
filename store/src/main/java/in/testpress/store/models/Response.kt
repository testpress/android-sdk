package `in`.testpress.store.models

import `in`.testpress.database.CoursesItem
import `in`.testpress.database.ProductsItem
import `in`.testpress.database.ProductsListEntity

data class ProductsListResponse(
	val next: String? = null,
	val perPage: Int? = null,
	val previous: String? = null,
	val count: Int? = null,
	val results: Result? = null
)

data class Result(
	val courses: List<CoursesItem?>? = null,
	val prices: List<PricesItem?>? = null,
	val products: List<ProductsItem?>? = null
)

fun Result.asDatabaseModel(): ProductsListEntity {
	return ProductsListEntity(
			courses = this.courses as List<CoursesItem>,
			products = this.products as List<ProductsItem>
	)
}


