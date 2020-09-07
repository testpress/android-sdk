package `in`.testpress.store.models

import `in`.testpress.database.Course
import `in`.testpress.database.Orders
import `in`.testpress.database.PricesItem
import `in`.testpress.database.ProductDetailEntity

data class ProductDetailResponse(
    var image: String? = null,
    var courses: List<Courses?>? = null,
    var surl: String? = null,
    var title: String? = null,
    var furl: String? = null,
    var id: Int? = null,
    var order: Order? = null,
    var prices: List<Prices?>? = null,
    var slug: String? = null,
    var endDate: String? = null,
    var paymentLink: String? = null,
    var buyNowText: String? = null,
    var descriptionHtml: String? = null,
    var currentPrice: String? = null,
    var startDate: String? = null
)

data class Order(
    var date: String? = null,
    var voucher: Voucher? = null,
    var checksum: String? = null,
    var id: Int? = null,
    var zip: String? = null,
    var amount: String? = null,
    var apikey: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var name: String? = null,
    var user: Int? = null,
    var status: String? = null,
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

data class Courses(
    var image: String? = null,
    var examsCount: Int? = null,
    var created: String? = null,
    var description: String? = null,
    var title: String? = null,
    var chaptersCount: Int? = null,
    var deviceAccessControl: String? = null,
    var createdBy: Int? = null,
    var enableDiscussions: Boolean? = null,
    var url: String? = null,
    var contentsCount: Int? = null,
    var contentsUrl: String? = null,
    var chaptersUrl: String? = null,
    var modified: String? = null,
    var videosCount: Int? = null,
    var externalContentLink: String? = null,
    var id: Int? = null,
    var attachmentsCount: Int? = null,
    var slug: String? = null,
    var htmlContentsCount: Int? = null,
    var order: Int? = null,
    var externalLinkLabel: String? = null
)

data class Prices(
    var id: Int? = null,
    var name: String? = null,
    var price: String? = null,
    var validity: Int? = null,
    var endDate: String? = null,
    var startDate: String? = null
)

fun ProductDetailResponse.asDatabaseModel(): ProductDetailEntity {
    return ProductDetailEntity(
            image = this.image,
            id = this.id,
            courses = this.courses as  List<Course?>,
            surl = this.surl,
            title = this.title,
            furl = this.furl,
            order = this.order as Orders,
            prices = this.prices as List<PricesItem?>,
            slug = this.slug,
            endDate = this.endDate,
            paymentLink = this.paymentLink,
            buyNowText = this.buyNowText,
            descriptionHtml = this.descriptionHtml,
            currentPrice = this.currentPrice,
            startDate = this.startDate
    )
}
