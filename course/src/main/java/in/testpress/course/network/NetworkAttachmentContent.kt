package `in`.testpress.course.network

import `in`.testpress.models.greendao.Attachment

data class NetworkAttachmentContent(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val attachmentUrl: String = "",
    val isRenderable: Boolean? = null
)

fun NetworkAttachmentContent.asGreenDaoModel(): Attachment {
    return Attachment(
         this.title, this.attachmentUrl, this.description, this.id, this.isRenderable
    )
}
