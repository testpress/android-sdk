package `in`.testpress.course.util

import android.content.Context
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.services.NewOfflineAttachmentDownloadManager
import `in`.testpress.util.FileDownloader
import `in`.testpress.util.getFileExtensionFromUrl
import `in`.testpress.util.sanitizeFileName

object AttachmentDownloader {

    fun downloadAttachment(context: Context, attachment: DomainAttachmentContent) {
        if (TestpressSdk.isUseInAppAttachmentDownload()) {
            NewOfflineAttachmentDownloadManager.getInstance(context).enqueueDownload(context, attachment)
        } else {
            val url = attachment.attachmentUrl
            if (url.isNullOrBlank()) {
                return
            }
            val title = attachment.title?.takeIf { it.isNotBlank() } ?: "Attachment-${attachment.id}"
            val fileName = "$title${getFileExtensionFromUrl(url)}".sanitizeFileName()
            FileDownloader(context).downloadFile(url, fileName)
        }
    }
}