package `in`.testpress.course.viewmodels

import android.app.Application
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.course.services.DownloadItem
import `in`.testpress.course.services.DownloadQueueManager
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.util.deleteFile
import `in`.testpress.util.openFile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OfflineAttachmentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = TestpressDatabase.invoke(application).offlineAttachmentDao()
    private val repo = OfflineAttachmentsRepository(dao)

    val files = repo.getAllFiles().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun getOfflineAttachment(id: Long) = repo.getAttachment(id)

    fun requestDownload(
        attachment: DomainAttachmentContent,
        destinationPath: String
    ) {
        if (attachment.attachmentUrl.isNullOrEmpty()) return
        val file = OfflineAttachment(
            id = attachment.id,
            title = attachment.title ?: "attachment_${attachment.id}",
            // If thr url is null, it will be empty string and download will be failed
            url = attachment.attachmentUrl,
            path = destinationPath,
            contentUri = null,
            downloadId = -1,
            status = OfflineAttachmentDownloadStatus.QUEUED
        )
        viewModelScope.launch {
            repo.insert(file)
            DownloadQueueManager.enqueue(DownloadItem(file.id, file.url, file.path))
        }
    }

    fun cancel(offlineAttachment: OfflineAttachment) {
        DownloadQueueManager.cancelDownloadById(offlineAttachment.id)
    }

    fun delete(offlineAttachment: OfflineAttachment) {
        viewModelScope.launch {
            val attachment = repo.getAttachmentById(offlineAttachment.downloadId)
            repo.delete(offlineAttachment.id)
            attachment?.let {
                deleteFile(it.path)
            }
        }
    }

    fun openFile(context: Context, file: OfflineAttachment) = openFile(context, file.path)

    companion object {
        fun get(context: FragmentActivity): OfflineAttachmentViewModel {
            return ViewModelProvider(context, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OfflineAttachmentViewModel(context.application) as T
                }
            }).get(OfflineAttachmentViewModel::class.java)
        }
    }
}

