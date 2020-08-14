package `in`.testpress.store.models

import com.google.gson.annotations.SerializedName

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

        @field:SerializedName("end_date")
        val endDate: String? = null,

        @field:SerializedName("payment_link")
        val paymentLink: String? = null,

        @field:SerializedName("buy_now_text")
        val buyNowText: String? = null,

        @field:SerializedName("description_html")
        val descriptionHtml: String? = null,

        @field:SerializedName("current_price")
        val currentPrice: String? = null,

        @field:SerializedName("start_date")
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