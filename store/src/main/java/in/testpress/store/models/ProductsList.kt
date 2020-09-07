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

data class CoursesItem(
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
	val id: Int? = null,
	val attachmentsCount: Int? = null,
	val slug: String? = null,
	val htmlContentsCount: Int? = null,
	val order: Int? = null,
	val externalLinkLabel: String? = null
)
