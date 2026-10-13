package `in`.testpress.course

import android.content.Context
import android.util.Log
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.course.services.NewOfflineAttachmentDownloadManager
import `in`.testpress.models.greendao.Attachment
import `in`.testpress.util.FileDownloader
import `in`.testpress.util.getFileExtensionFromUrl
import `in`.testpress.util.sanitizeFileName

private const val TAG = "TestpressDownload"

@JvmOverloads
fun downloadAttachment(
    context: Context,
    attachment: Attachment,
    enableLocalFileDownload: Boolean = true
) {
    downloadAttachment(context, attachment.asDomainContent(), enableLocalFileDownload)
}

@JvmOverloads
fun downloadAttachment(
    context: Context,
    attachment: DomainAttachmentContent,
    enableLocalFileDownload: Boolean = true
) {
    val attachmentUrl = attachment.attachmentUrl
    if (attachmentUrl.isNullOrBlank()) {
        Log.w(TAG, "Attachment URL is missing; cannot download attachment id=${attachment.id}")
        return
    }

    if (enableLocalFileDownload) {
        startLocalStorageDownload(context, attachment)
    } else {
        startInAppDownload(context, attachment)
    }
}

private fun startLocalStorageDownload(context: Context, attachment: DomainAttachmentContent) {
    val url = attachment.attachmentUrl ?: return
    try {
        val title = attachment.title?.takeIf { it.isNotBlank() } ?: "Attachment-${attachment.id}"
        val extension = getFileExtensionFromUrl(url)
        val fileName = "$title$extension".sanitizeFileName()
        FileDownloader(context).downloadFile(url, fileName)
    } catch (e: Exception) {
        Log.e(TAG, "Local-storage download failed for attachment id=${attachment.id}", e)
    }
}

private fun startInAppDownload(context: Context, attachment: DomainAttachmentContent) {
    try {
        val downloadManager = NewOfflineAttachmentDownloadManager.getInstance(context)
        downloadManager.requestOrNotify(context.applicationContext, attachment, openIfCompleted = true)
    } catch (e: Exception) {
        Log.e(TAG, "In-app download failed to start for attachment id=${attachment.id}", e)
    }
}
