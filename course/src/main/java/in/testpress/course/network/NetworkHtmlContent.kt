package `in`.testpress.course.network

import `in`.testpress.database.HtmlContentEntity
import `in`.testpress.models.greendao.HtmlContent

data class NetworkHtmlContent(
    val id: Long,
    val title: String = "",
    val readTime: String? = null,
    val textHtml: String = ""
)

fun NetworkHtmlContent.asGreenDaoModel(): HtmlContent {
    return HtmlContent(
        this.id, this.title, this.textHtml, null, this.readTime
    )
}

fun NetworkHtmlContent.asDatabaseModel(): HtmlContentEntity {
    return HtmlContentEntity(
        id = id,
        title = title,
        textHtml = textHtml,
        readTime = readTime,
        sourceUrl = null
    )
}
