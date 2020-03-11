package `in`.testpress.course.domain

import `in`.testpress.database.AttachmentEntity
import `in`.testpress.models.greendao.Attachment

data class DomainAttachmentContent(
    val id: Long,
    val title: String? = null,
    val attachmentUrl: String? = null,
    val description: String? = null
)


fun createDomainAttachmentContent(attachment: AttachmentEntity): DomainAttachmentContent {
    return DomainAttachmentContent(
        id = attachment.id,
        title = attachment.title,
        attachmentUrl = attachment.attachmentUrl,
        description = attachment.description
    )
}

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

fun AttachmentEntity.asDomainAttachment(): DomainAttachmentContent {
    return createDomainAttachmentContent(this)
}