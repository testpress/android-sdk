package `in`.testpress.store.models

data class CouponCodeResponse(
    val phone: String? = null,
    val email: String? = null,
    val id: Int? = null,
    val user: Int? = null,
    val amount: String? = null,
    val apikey: String? = null,
    val zip: String? = null,
    val status: String? = null,
    val voucher: Voucher? = null,
    val name: String? = null,
    val date: String? = null,
    val checksum: String? = null,
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

data class Voucher(
    val code: String? = null
)

data class OrderItemsItem(
    val product: String? = null,
    val quantity: Int? = null,
    val price: String? = null
)
