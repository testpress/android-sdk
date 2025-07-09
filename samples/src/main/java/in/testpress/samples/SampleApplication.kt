package `in`.testpress.samples

import android.app.Application
import android.util.Log
import `in`.testpress.course.helpers.OfflineAttachmentSyncManager
import `in`.testpress.database.TestpressDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        syncDownloads()
    }

    private fun syncDownloads() {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val dao = TestpressDatabase.invoke(this@SampleApplication).offlineAttachmentDao()
                val syncManager = OfflineAttachmentSyncManager(this@SampleApplication, dao)
                syncManager.syncDownloads()
            } catch (e: Exception) {
                Log.e("SampleApplication", "Failed to sync downloads", e)
            }
        }
    }
}