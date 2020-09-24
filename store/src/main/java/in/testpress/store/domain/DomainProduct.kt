package `in`.testpress.store.domain

import `in`.testpress.database.ContentEntity
import `in`.testpress.database.CourseEntity
import `in`.testpress.database.ProductEntity
import `in`.testpress.database.ProductWithCourses

data class DomainProduct(
    var endDate: String? = null,
    var image: String? = null,
    var surl: String? = null,
    var title: String? = null,
    var paymentLink: String? = null,
    var buyNowText: String? = null,
    var furl: String? = null,
    var id: Int? = null,
    var descriptionHtml: String? = null,
    var currentPrice: String? = null,
//    var prices: List<Int?>? = null,
    var slug: String? = null,
    var startDate: String? = null
)

data class DomainCourse(
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

data class DomainPrice(
    var id: Int? = null,
    var name: String? = null,
    var price: String? = null,
    var validity: Int? = null,
    var endDate: String? = null,
    var startDate: String? = null
)

data class DomainProductWithCourse(
    var product: DomainProduct? = null,
    var courses: List<DomainCourse>? = null
)


fun createDomainProduct(productEntity: ProductEntity): DomainProduct {
    return DomainProduct(
        endDate = productEntity.endDate,
        image = productEntity.image,
        surl = productEntity.surl,
        title = productEntity.title,
        paymentLink = productEntity.paymentLink,
        buyNowText = productEntity.buyNowText,
        furl = productEntity.furl,
        id = productEntity.id,
        descriptionHtml = productEntity.descriptionHtml,
        currentPrice = productEntity.currentPrice,
//        prices = productEntity.prices,
        slug = productEntity.slug,
        startDate = productEntity.startDate
    )
}

fun ProductEntity.asDomainContent(): DomainProduct {
    return createDomainProduct(this)
}

@JvmName("asDomainContentProductEntity")
fun List<ProductEntity>.asDomainContent(): List<DomainProduct> {
    return this.map {
        createDomainProduct(it)
    }
}

fun createDomainCourse(courseEntity: CourseEntity): DomainCourse {
    return DomainCourse(
    image = courseEntity.image,
    examsCount = courseEntity.examsCount,
    created = courseEntity.created,
    description = courseEntity.description,
    title = courseEntity.title,
    chaptersCount = courseEntity.chaptersCount,
    deviceAccessControl = courseEntity.deviceAccessControl,
    createdBy = courseEntity.createdBy,
    enableDiscussions = courseEntity.enableDiscussions,
    url = courseEntity.url,
    contentsCount = courseEntity.contentsCount,
    contentsUrl = courseEntity.contentsUrl,
    chaptersUrl = courseEntity.chaptersUrl,
    modified  = courseEntity.modified,
    videosCount = courseEntity.videosCount,
    externalContentLink = courseEntity.externalContentLink,
    id = courseEntity.id
//    attachmentsCount = courseEntity.attachmentsCount,
//    slug = courseEntity.slug,
//    htmlContentsCount = courseEntity.htmlContentsCount,
//    order = courseEntity.order,
//    externalLinkLabel = courseEntity.externalLinkLabel,
    )
}

fun CourseEntity.asDomainContent(): DomainCourse {
    return createDomainCourse(this)
}

@JvmName("asDomainContentCourseEntity")
fun List<CourseEntity>.asDomainContent(): List<DomainCourse> {
    return this.map {
        createDomainCourse(it)
    }
}

fun createDomainProductWithCourse(productWithCourses: ProductWithCourses): DomainProductWithCourse {
    return DomainProductWithCourse(
            product = productWithCourses.product.asDomainContent(),
            courses = productWithCourses.courses.asDomainContent()
    )
}

fun ProductWithCourses.asDomainContent(): DomainProductWithCourse {
    return createDomainProductWithCourse(this)
}

fun List<ProductWithCourses>.asDomainContent(): List<DomainProductWithCourse> {
    return this.map {
        createDomainProductWithCourse(it)
    }
}