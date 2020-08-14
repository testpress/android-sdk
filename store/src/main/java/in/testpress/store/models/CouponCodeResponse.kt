package `in`.testpress.store.models

import com.google.gson.annotations.SerializedName

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

        @field:SerializedName("amount_without_discounts")
        val amountWithoutDiscounts: String? = null,

        @field:SerializedName("access_code")
        val accessCode: String? = null,

        @field:SerializedName("product_info")
        val productInfo: String? = null,

        @field:SerializedName("pg_url")
        val pgUrl: String? = null,

        @field:SerializedName("land_mark")
        val landMark: String? = null,

        @field:SerializedName("amount_without_processing_fee")
        val amountWithoutProcessingFee: String? = null,

        @field:SerializedName("shipping_address")
        val shippingAddress: String? = null,

        @field:SerializedName("order_items")
        val orderItems: List<OrderItemsItem?>? = null,

        @field:SerializedName("ip_address")
        val ipAddress: String? = null,

        @field:SerializedName("enc_data")
        val encData: String? = null,

        @field:SerializedName("uses_testpress_pg")
        val usesTestpressPg: Boolean? = null,

        @field:SerializedName("order_id")
        val orderId: String? = null,

        @field:SerializedName("mobile_sdk_hash")
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
