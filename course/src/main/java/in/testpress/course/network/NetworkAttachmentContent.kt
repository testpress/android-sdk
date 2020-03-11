package `in`.testpress.course.network

import `in`.testpress.database.AttachmentEntity
import `in`.testpress.models.greendao.Attachment

data class NetworkAttachmentContent(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val attachmentUrl: String = ""
)

fun NetworkAttachmentContent.asGreenDaoModel(): Attachment {
    return Attachment(
         this.title, this.attachmentUrl, this.description, this.id
    )
}

fun NetworkAttachmentContent.asDatabaseModel(): AttachmentEntity {
    return AttachmentEntity(
        id = id,
        title = title,
        description = description,
        attachmentUrl = attachmentUrl
    )
}