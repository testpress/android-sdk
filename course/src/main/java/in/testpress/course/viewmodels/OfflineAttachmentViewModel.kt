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
import `in`.testpress.course.services.NewOfflineAttachmentDownloadManager
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachment
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

    /**
     * Submits an attachment download request.
     *
     * @param onDecision Optional callback invoked with the actual [NewOfflineAttachmentDownloadManager.RequestDecision]
     *                   once the mutex-protected database check resolves. Dispatched on the main thread ([Dispatchers.Main]).
     * @return `true` if the request was submitted for processing; `false` if rejected immediately.
     */
    @JvmOverloads
    fun requestDownload(
        context: Context,
        attachment: DomainAttachmentContent,
        onDecision: ((NewOfflineAttachmentDownloadManager.RequestDecision) -> Unit)? = null
    ): Boolean {
        return NewOfflineAttachmentDownloadManager.getInstance(getApplication()).enqueueDownload(context, attachment, onDecision)
    }

    suspend fun requestDownloadAwait(
        context: Context,
        attachment: DomainAttachmentContent
    ): NewOfflineAttachmentDownloadManager.RequestDecision {
        return NewOfflineAttachmentDownloadManager.getInstance(getApplication()).enqueueDownloadAwait(context, attachment)
    }

    fun requestOrNotify(
        context: Context,
        attachment: DomainAttachmentContent,
        openIfCompleted: Boolean = false
    ) {
        NewOfflineAttachmentDownloadManager.getInstance(getApplication()).requestOrNotify(context, attachment, openIfCompleted)
    }

    fun cancel(context: Context, offlineAttachment: OfflineAttachment) {
        NewOfflineAttachmentDownloadManager.getInstance(getApplication()).cancelDownload(context, offlineAttachment)
    }

    fun delete(context: Context, offlineAttachment: OfflineAttachment) {
        NewOfflineAttachmentDownloadManager.getInstance(getApplication()).deleteDownload(context, offlineAttachment)
    }

    fun openFile(context: Context, offlineAttachment: OfflineAttachment) {
        NewOfflineAttachmentDownloadManager.getInstance(getApplication()).openFile(context, offlineAttachment)
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            repo.delete(id)
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
}


