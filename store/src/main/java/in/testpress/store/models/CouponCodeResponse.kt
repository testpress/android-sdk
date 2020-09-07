package `in`.testpress.store.models

data class CouponCodeResponse(
    var phone: String? = null,
    var email: String? = null,
    var id: Int? = null,
    var user: Int? = null,
    var amount: String? = null,
    var apikey: String? = null,
    var zip: String? = null,
    var status: String? = null,
    var voucher: Voucher? = null,
    var name: String? = null,
    var date: String? = null,
    var checksum: String? = null,
    var amountWithoutDiscounts: String? = null,
    var accessCode: String? = null,
    var productInfo: String? = null,
    var pgUrl: String? = null,
    var landMark: String? = null,
    var amountWithoutProcessingFee: String? = null,
    var shippingAddress: String? = null,
    var orderItems: List<OrderItems?>? = null,
    var ipAddress: String? = null,
    var encData: String? = null,
    var usesTestpressPg: Boolean? = null,
    var orderId: String? = null,
    var mobileSdkHash: String? = null
)

data class Voucher(
    var code: String? = null
)

data class OrderItems(
    var product: String? = null,
    var quantity: Int? = null,
    var price: String? = null
)
