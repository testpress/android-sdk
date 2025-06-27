package `in`.testpress.course.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
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
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.util.openFile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

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
        context: Context,
        attachment: DomainAttachmentContent,
        destinationPath: String,
        fileName: String
    ) {
        if (attachment.attachmentUrl.isNullOrEmpty()) return
        val file = OfflineAttachment(
            id = attachment.id,
            title = attachment.title ?: "attachment_${attachment.id}",
            // If thr url is null, it will be empty string and download will be failed
            url = attachment.attachmentUrl,
            path = destinationPath,
            fileName = fileName,
            contentUri = null,
            status = OfflineAttachmentDownloadStatus.QUEUED
        )
        viewModelScope.launch {
            repo.insert(file)
            DownloadQueueManager.enqueue(context, DownloadItem(file.id, file.url, file.path))
        }
    }

    fun cancel(id: Long) {
        DownloadQueueManager.cancelDownloadById(id)
    }

    fun delete(context: Context, id: Long) {
        viewModelScope.launch {
            val attachment = repo.getAttachmentById(id)
            repo.delete(id)
            attachment?.deleteFile(context)
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

fun OfflineAttachment.deleteFile(context: Context) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        val resolver = context.contentResolver
        contentUri?.let {
            resolver.delete(Uri.parse(it), null, null)
        }
    } else {
        val file = File(path)
        if (file.exists()) {
            try {
                file.delete()
            } catch (e: Exception) {
                Log.e("AttachmentDelete", "Failed to delete file: ${file.absolutePath}", e)
            }
        }
    }
}

