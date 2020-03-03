package `in`.testpress.course.domain

import `in`.testpress.models.greendao.Attachment

data class DomainAttachmentContent(
    val id: Long,
    val title: String? = null,
    val attachmentUrl: String? = null,
    val description: String? = null
)


fun createDomainAttachmentContent(attachment: Attachment): DomainAttachmentContent {
    return DomainAttachmentContent(
        id = attachment.id,
        title = attachment.title,
        attachmentUrl = attachment.attachmentUrl,
        description = attachment.description
    )
}

fun Attachment.asDomainAttachment(): DomainAttachmentContent {
    return createDomainAttachmentContent(this)
}