package `in`.testpress.course.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.course.services.DownloadQueueManager
import `in`.testpress.course.util.FileUtils.openPdf
import `in`.testpress.course.util.FileUtils.sharePdf
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.OfflineAttachment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class OfflineAttachmentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = TestpressDatabase.invoke(application).offlineAttachmentDao()
    private val repo = OfflineAttachmentsRepository(dao)
    private val queueManager = DownloadQueueManager(viewModelScope, repo)

    val files = repo.getAllFiles().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun requestDownload(file: OfflineAttachment) = queueManager.enqueue(file)
    fun pause(id: String) = Unit // Extend later
    fun cancel(id: String) = Unit // Extend later
    fun openFile(context: Context, file: OfflineAttachment) = openPdf(context, file.path)
    fun shareFile(context: Context, file: OfflineAttachment) = sharePdf(context, file.path)
}