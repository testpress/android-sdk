package `in`.testpress.course.domain

import androidx.core.net.toUri
import `in`.testpress.models.greendao.Attachment

data class DomainAttachmentContent(
    val id: Long,
    val title: String? = null,
    val attachmentUrl: String? = null,
    val description: String? = null,
    val isRenderable: Boolean? = null
) {

    fun isAttachmentUrlExpired(): Boolean {
        attachmentUrl ?: return true
        val uri = attachmentUrl.toUri()
        val expires = uri.getQueryParameter("Expires")?.toLongOrNull() ?: return true
        val currentTime = System.currentTimeMillis() / 1000
        return currentTime > expires
    }
}


fun createDomainAttachmentContent(attachment: Attachment): DomainAttachmentContent {
    return DomainAttachmentContent(
        id = attachment.id,
        title = attachment.title,
        attachmentUrl = attachment.attachmentUrl,
        description = attachment.description,
        isRenderable = attachment.isRenderable
    )
}

fun Attachment.asDomainContent(): DomainAttachmentContent {
    return createDomainAttachmentContent(this)
}