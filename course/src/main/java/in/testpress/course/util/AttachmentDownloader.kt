package `in`.testpress.course.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AttachmentDownloader {

    @Volatile
    private var repository: OfflineAttachmentsRepository? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.w("AttachmentDownloader", "Attachment download failed", throwable)
    }

    fun downloadAttachment(context: Context, attachment: DomainAttachmentContent) {
        val url = attachment.attachmentUrl
        if (url.isNullOrBlank()) {
            ViewUtils.toast(context, context.getString(R.string.download_url_not_available))
            return
        }

        if (TestpressSdk.isUseInAppAttachmentDownload()) {
            if (attachment.allowDownload != true) {
                ViewUtils.toast(context, context.getString(R.string.download_not_allowed))
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
        val appContext = context.applicationContext

        val lifecycleOwner = context as? LifecycleOwner
        if (lifecycleOwner != null) {
            lifecycleOwner.lifecycleScope.launch(exceptionHandler) {
                enqueueAndShowSnackbar(context, appContext, attachment)
            }
        } else {
            val callScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)
            callScope.launch {
                try {
                    enqueueAndShowSnackbar(context, appContext, attachment)
                } finally {
                    callScope.cancel()
                }
            }
        }
    }

    private suspend fun enqueueAndShowSnackbar(
        context: Context,
        appContext: Context,
        attachment: DomainAttachmentContent
    ) {
        val messageRes = try {
            val status = withContext(Dispatchers.IO) {
                getRepository(appContext).getById(attachment.id)?.status
            }
            when (status) {
                OfflineAttachmentDownloadStatus.COMPLETED ->
                    R.string.already_downloaded_check_downloads
                OfflineAttachmentDownloadStatus.QUEUED,
                OfflineAttachmentDownloadStatus.DOWNLOADING ->
                    R.string.download_in_progress_check_downloads
                else -> when (NewOfflineAttachmentDownloadManager.getInstance(appContext)
                    .enqueueDownload(appContext, attachment)) {
                    NewOfflineAttachmentDownloadManager.EnqueueResult.Started ->
                        R.string.download_started_check_downloads
                    NewOfflineAttachmentDownloadManager.EnqueueResult.AlreadyQueued ->
                        R.string.download_in_progress_check_downloads
                    NewOfflineAttachmentDownloadManager.EnqueueResult.Rejected ->
                        R.string.download_start_error
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("AttachmentDownloader", "Attachment download enqueue failed", e)
            R.string.download_start_error
        }
        withContext(Dispatchers.Main) {
            showDownloadSnackbar(context, context.getString(messageRes))
        }
    }

    private fun getRepository(context: Context): OfflineAttachmentsRepository {
        return repository ?: synchronized(this) {
            repository ?: OfflineAttachmentsRepository(
                TestpressDatabase.invoke(context.applicationContext).offlineAttachmentDao()
            ).also { repository = it }
        }
    }

    private fun showDownloadSnackbar(context: Context, message: String) {
        val activity = context as? Activity
        val rootView = activity?.findViewById<View>(android.R.id.content)
        if (activity != null && rootView != null &&
            !activity.isFinishing && !activity.isDestroyed) {
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.downloads) {
                    activity.startActivity(
                        DownloadsActivity.createIntent(activity)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                .show()
        } else {
            ViewUtils.toast(context, message)
        }
    }
}
