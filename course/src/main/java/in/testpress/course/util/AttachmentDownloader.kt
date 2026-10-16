package `in`.testpress.course.util

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.course.services.NewOfflineAttachmentDownloadManager
import `in`.testpress.course.ui.DownloadsActivity
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.util.FileDownloader
import `in`.testpress.util.ViewUtils
import `in`.testpress.util.getFileExtensionFromUrl
import `in`.testpress.util.sanitizeFileName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AttachmentDownloader {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun downloadAttachment(context: Context, attachment: DomainAttachmentContent) {
        val url = attachment.attachmentUrl
        if (url.isNullOrBlank()) {
            ViewUtils.toast(context, context.getString(R.string.download_url_not_available))
            return
        }

        if (TestpressSdk.isUseInAppAttachmentDownload()) {
            if (attachment.allowDownload != true) {
                ViewUtils.toast(context, "Download not allowed")
                return
            }
            enqueueAndNotify(context, attachment)
        } else {
            val title = attachment.title?.takeIf { it.isNotBlank() } ?: "Attachment-${attachment.id}"
            val fileName = "$title${getFileExtensionFromUrl(url)}".sanitizeFileName()
            FileDownloader(context).downloadFile(url, fileName)
        }
    }

    private fun enqueueAndNotify(context: Context, attachment: DomainAttachmentContent) {
        val dao = TestpressDatabase.invoke(context).offlineAttachmentDao()
        val repository = OfflineAttachmentsRepository(dao)

        scope.launch {
            val existing = repository.getById(attachment.id)
            val messageRes = when (existing?.status) {
                OfflineAttachmentDownloadStatus.COMPLETED ->
                    R.string.already_downloaded_check_downloads
                OfflineAttachmentDownloadStatus.QUEUED,
                OfflineAttachmentDownloadStatus.DOWNLOADING ->
                    R.string.download_in_progress_check_downloads
                else ->
                    R.string.download_started_check_downloads
            }

            NewOfflineAttachmentDownloadManager.getInstance(context)
                .enqueueDownload(context, attachment)

            withContext(Dispatchers.Main) {
                showDownloadSnackbar(context, context.getString(messageRes))
            }
        }
    }

    private fun showDownloadSnackbar(context: Context, message: String) {
        val rootView = (context as? Activity)?.findViewById<View>(android.R.id.content)
        if (rootView != null) {
            val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .setAction(context.getString(R.string.downloads)) {
                    context.startActivity(DownloadsActivity.createIntent(context))
                }

            snackbar.show()
        } else {
            ViewUtils.toast(context, message)
        }
    }
}
