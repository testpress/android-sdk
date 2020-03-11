package `in`.testpress.course.repository

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.asGreenDaoModel
import android.content.Context

class AttachmentContentRepository(context: Context) : ContentRepository(context) {
    val attachmentDao = TestpressSDKDatabase.getAttachmentDao(context)

    override fun storeContentAndItsRelationsToDB(content: NetworkContent) {
        val greenDaoContent = content.asGreenDaoModel()
        content.attachment?.let {
            val attachment = it.asGreenDaoModel()
            greenDaoContent.attachmentId = attachment.id
            attachmentDao.insertOrReplace(it.asGreenDaoModel())
        }
        contentDao.insertOrReplace(greenDaoContent)
    }
}