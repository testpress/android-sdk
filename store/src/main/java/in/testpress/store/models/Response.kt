package `in`.testpress.store.models

data class Response(
	val next: String? = null,
	val perPage: Int? = null,
	val previous: String? = null,
	val count: Int? = null,
	val results: Result? = null
)

data class Result(
	val courses: List<Courses?>? = null,
	val prices: List<Prices?>? = null,
	val products: List<Products?>? = null
)

data class Prices(
	val endDate: String? = null,
	val price: String? = null,
	val name: String? = null,
	val id: Int? = null,
	val validity: Any? = null,
	val startDate: String? = null
)

data class Products(
	val endDate: Any? = null,
	val image: String? = null,
	val courses: List<Any?>? = null,
	val surl: Any? = null,
	val title: String? = null,
	val paymentLink: String? = null,
	val buyNowText: String? = null,
	val furl: Any? = null,
	val id: Int? = null,
	val descriptionHtml: String? = null,
	val currentPrice: String? = null,
	val prices: List<Int?>? = null,
	val slug: String? = null,
	val startDate: Any? = null
)

data class Courses(
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

