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
import `in`.testpress.course.services.OfflineAttachmentDownloadManager
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

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
        attachment: DomainAttachmentContent
    ) {
        OfflineAttachmentDownloadManager.getInstance().enqueueDownload(context, attachment)
    }

    fun cancel(context: Context, offlineAttachment: OfflineAttachment) {
        OfflineAttachmentDownloadManager.getInstance().cancelDownload(context, offlineAttachment)
    }

    fun delete(context: Context, offlineAttachment: OfflineAttachment) {
        OfflineAttachmentDownloadManager.getInstance().deleteDownload(context, offlineAttachment)
    }

    fun openFile(context: Context, offlineAttachment: OfflineAttachment) {
        OfflineAttachmentDownloadManager.getInstance().openFile(context, offlineAttachment)
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
}

fun OfflineAttachment.deleteFile(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        contentUri?.let {
            try {
                resolver.delete(Uri.parse(it), null, null)
            } catch (e: Exception) {
                Log.e("AttachmentDelete", "Failed to delete file: $it", e)
            }
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

fun OfflineAttachment.openFile(context: Context) {
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        if (contentUri.isNullOrBlank()) {
            Toast.makeText(context, "File not found.", Toast.LENGTH_SHORT).show()
            return
        }
        Uri.parse(contentUri)
    } else {
        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(context, "File not found.", Toast.LENGTH_SHORT).show()
            return
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.testpressFileProvider",
            file
        )
    }

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri))
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this file type.", Toast.LENGTH_SHORT).show()
    }
}


