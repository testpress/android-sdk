package `in`.testpress.store.network

import `in`.testpress.database.CourseEntity

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


fun NetworkCourse.asDatabaseModel(): CourseEntity {
    return CourseEntity(
        image = this.image,
        examsCount = this.examsCount,
        created = this.created,
        description = this.description,
        title = this.title,
        chaptersCount = this.chaptersCount,
        deviceAccessControl = this.deviceAccessControl,
        createdBy = this.createdBy,
        enableDiscussions = this.enableDiscussions,
        url = this.url,
        contentsCount = this.contentsCount,
        contentsUrl = this.contentsUrl,
        chaptersUrl = this.chaptersUrl,
        modified = this.modified,
        videosCount = this.videosCount,
        externalContentLink = this.externalContentLink,
        id = this.id,
        attachmentsCount = this.attachmentsCount,
        slug = this.slug,
        htmlContentsCount = this.htmlContentsCount,
        order = this.order,
        externalLinkLabel = this.externalLinkLabel
    )
}


data class NetworkPrice(
    var id: Int? = null,
    var name: String? = null,
    var price: String? = null,
    var validity: Int? = null,
    var endDate: String? = null,
    var startDate: String? = null
)

