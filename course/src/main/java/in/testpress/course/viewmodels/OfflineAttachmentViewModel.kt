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
import `in`.testpress.course.services.DownloadQueueManager
import `in`.testpress.course.util.FileUtils.openPdf
import `in`.testpress.course.util.FileUtils.sharePdf
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class OfflineAttachmentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = TestpressDatabase.invoke(application).offlineAttachmentDao()
    private val repo = OfflineAttachmentsRepository(dao)
    private val queueManager = DownloadQueueManager(CoroutineScope(Dispatchers.IO), repo)

    val files = repo.getAllFiles().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun requestDownload(attachment: DomainAttachmentContent, destinationPath: String) {
        val file = OfflineAttachment(
            id = attachment.id,
            title = attachment.title!!,
            url = attachment.attachmentUrl!!,
            path = destinationPath,
            status = OfflineAttachmentDownloadStatus.QUEUED
        )
        viewModelScope.launch {
            repo.insert(file)
            queueManager.enqueue(file)
        }
    }
    fun cancel(id: Long) = Unit
    fun delete(id: Long) = Unit
    fun openFile(context: Context, file: OfflineAttachment) = openPdf(context, file.path)
    fun shareFile(context: Context, file: OfflineAttachment) = sharePdf(context, file.path)

    fun isAttachmentDownloaded(attachmentId: Long): OfflineAttachment? {
        // It's generally not recommended to block the main thread.
        // Consider making this a suspend function or returning a Flow/LiveData
        // if you need to observe the download status asynchronously.
        return runBlocking(Dispatchers.IO) {
            val attachment = repo.getAttachmentById(attachmentId)
            return@runBlocking if (attachment?.status == OfflineAttachmentDownloadStatus.DOWNLOADED) {
                attachment
            } else {
                null
            }
        }
    }

    companion object {
        fun get(context: FragmentActivity) : OfflineAttachmentViewModel {
            return ViewModelProvider(context, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OfflineAttachmentViewModel(context.application) as T
                }
            }).get(OfflineAttachmentViewModel::class.java)
        }
    }
}