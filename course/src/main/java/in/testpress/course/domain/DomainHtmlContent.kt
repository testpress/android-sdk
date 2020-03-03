package `in`.testpress.course.domain

import `in`.testpress.models.greendao.HtmlContent

data class DomainHtmlContent(
    val id: Long,
    val title: String? = null,
    val textHtml: String? = null,
    val sourceUrl: String? = null,
    val readTime: String? = null
)

fun createDomainHtmlContent(htmlContent: HtmlContent): DomainHtmlContent {
    return DomainHtmlContent(
        id = htmlContent.id,
        title = htmlContent.title,
        textHtml = htmlContent.textHtml,
        sourceUrl = htmlContent.sourceUrl,
        readTime = htmlContent.readTime
    )
}

fun HtmlContent.asDomainAttachment(): DomainHtmlContent {
    return createDomainHtmlContent(this)
}