package `in`.testpress.store.models

data class ProductDetailResponse(
    val image: String? = null,
    val courses: List<Course?>? = null,
    val surl: String? = null,
    val title: String? = null,
    val furl: String? = null,
    val id: Int? = null,
    val order: Orders? = null,
    val prices: List<PricesItem?>? = null,
    val slug: String? = null,
    val endDate: String? = null,
    val paymentLink: String? = null,
    val buyNowText: String? = null,
    val descriptionHtml: String? = null,
    val currentPrice: String? = null,
    val startDate: String? = null
)

data class Orders(
    val date: String? = null,
    val voucher: Voucher? = null,
    val checksum: String? = null,
    val id: Int? = null,
    val zip: String? = null,
    val amount: String? = null,
    val apikey: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val name: String? = null,
    val user: Int? = null,
    val status: String? = null,
    val amountWithoutDiscounts: String? = null,
    val accessCode: String? = null,
    val productInfo: String? = null,
    val pgUrl: String? = null,
    val landMark: String? = null,
    val amountWithoutProcessingFee: String? = null,
    val shippingAddress: String? = null,
    val orderItems: List<OrderItemsItem?>? = null,
    val ipAddress: String? = null,
    val encData: String? = null,
    val usesTestpressPg: Boolean? = null,
    val orderId: String? = null,
    val mobileSdkHash: String? = null
)

data class Course(
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

data class PricesItem(
    var id: Int? = null,
    var name: String? = null,
    var price: String? = null,
    var validity: Int? = null,
    var endDate: String? = null,
    var startDate: String? = null
)