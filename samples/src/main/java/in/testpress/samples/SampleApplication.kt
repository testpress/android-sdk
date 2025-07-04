package `in`.testpress.samples

import android.app.Application
import `in`.testpress.course.helpers.OfflineAttachmentSyncManager
import `in`.testpress.database.TestpressDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        syncDownloads()
    }

    private fun syncDownloads() {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = TestpressDatabase.invoke(this@SampleApplication)
                .offlineAttachmentDao()
            val syncManager =
                OfflineAttachmentSyncManager(this@SampleApplication, dao)
            syncManager.syncDownloads()
        }
    }
}

