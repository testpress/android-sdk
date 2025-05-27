package `in`.testpress.course.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.course.services.DownloadItem
import `in`.testpress.course.services.DownloadQueueManager
import `in`.testpress.course.util.FileUtils.deleteFile
import `in`.testpress.course.util.FileUtils.openPdf
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class OfflineAttachmentViewModel(application: Application) : AndroidViewModel(application),
    DownloadQueueManager.Callback {
    private val dao = TestpressDatabase.invoke(application).offlineAttachmentDao()
    private val repo = OfflineAttachmentsRepository(dao)

    init {
        DownloadQueueManager.setCallback(this)
    }

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
            DownloadQueueManager.enqueue(DownloadItem(file.id, file.url, file.path))
        }
    }

    fun cancel(id: Long) {
        DownloadQueueManager.cancelDownloadById(id)
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            val attachment = repo.getAttachmentById(id)
            repo.delete(id)
            attachment?.let {
                deleteFile(it.path)
            }
        }
    }

    fun openFile(context: Context, file: OfflineAttachment) = openPdf(context, file.path)

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
        fun get(context: FragmentActivity): OfflineAttachmentViewModel {
            return ViewModelProvider(context, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OfflineAttachmentViewModel(context.application) as T
                }
            }).get(OfflineAttachmentViewModel::class.java)
        }
    }

    override fun onDownloadStarted(item: DownloadItem) {
        viewModelScope.launch {
            repo.updateStatus(item.id, OfflineAttachmentDownloadStatus.DOWNLOADING)
        }
    }

    override fun onProgress(item: DownloadItem, progress: Int) {
        viewModelScope.launch {
            Log.d("TAG", "onProgress: $progress")
            repo.updateProgress(item.id, progress)
        }
    }

    override fun onDownloadCompleted(item: DownloadItem) {
        viewModelScope.launch {
            repo.updateStatus(item.id, OfflineAttachmentDownloadStatus.DOWNLOADED)
        }
    }

    override fun onDownloadFailed(item: DownloadItem, error: Throwable) {
        viewModelScope.launch {
            repo.updateStatus(item.id, OfflineAttachmentDownloadStatus.FAILED)
        }
    }

    override fun onDownloadCancelled(item: DownloadItem) {
        delete(item.id)
    }
}