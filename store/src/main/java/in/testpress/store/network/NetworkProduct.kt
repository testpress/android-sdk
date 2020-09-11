package `in`.testpress.store.network

data class NetworkProductResponse(
    var courses: List<NetworkCourse?>? = null,
    var prices: List<NetworkPrice?>? = null,
    var products: List<NetworkProduct?>? = null
)

data class NetworkProduct(
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

data class NetworkCourse(
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

data class NetworkPrice(
    var id: Int? = null,
    var name: String? = null,
    var price: String? = null,
    var validity: Int? = null,
    var endDate: String? = null,
    var startDate: String? = null
)

