package `in`.testpress.samples

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import `in`.testpress.course.helpers.OfflineAttachmentSyncManager
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.util.applySystemBarColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        syncDownloads()
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
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

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activity.applySystemBarColors(activity.window.decorView)
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}

    }
}