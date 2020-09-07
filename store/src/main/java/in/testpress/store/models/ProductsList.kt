package `in`.testpress.store.models

import `in`.testpress.database.CoursesItem
import `in`.testpress.database.ProductsItem
import `in`.testpress.database.ProductsListEntity

data class ProductListResponse(
	var next: String? = null,
	var perPage: Int? = null,
	var previous: String? = null,
	var count: Int? = null,
	var results: ProductsList? = null
)

data class ProductsList(
	var courses: List<CoursesItem>? = null,
	var prices: List<PricesItem>? = null,
	var products: List<ProductsItem>? = null
)

fun ProductsList.asDatabaseModel(): ProductsListEntity {
	return ProductsListEntity(
		courses = this.courses,
		products = this.products
	)
}
