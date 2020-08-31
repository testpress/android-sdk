package `in`.testpress.store.models

data class ProductsList(
	val next: String? = null,
	val perPage: Int? = null,
	val previous: String? = null,
	val count: Int? = null,
	val results: Results? = null
)

data class Results(
	val courses: List<CoursesItem?>? = null,
	val prices: List<PricesItem?>? = null,
	val products: List<ProductsItem?>? = null
)


data class ProductsItem(
	val endDate: String? = null,
	val image: String? = null,
	val courses: List<Int?>? = null,
	val surl: String? = null,
	val title: String? = null,
	val paymentLink: String? = null,
	val buyNowText: String? = null,
	val furl: String? = null,
	val id: Int? = null,
	val descriptionHtml: String? = null,
	val currentPrice: String? = null,
	val prices: List<Int?>? = null,
	val slug: String? = null,
	val startDate: String? = null
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
