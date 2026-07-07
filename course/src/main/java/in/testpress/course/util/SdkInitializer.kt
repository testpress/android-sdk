package `in`.testpress.course.util

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import `in`.testpress.course.repository.OfflineAttachmentsRepository
import `in`.testpress.course.services.OfflineAttachmentDownloadManager
import `in`.testpress.database.TestpressDatabase
import io.sentry.Sentry

class SdkInitializer : ContentProvider() {
    
    override fun onCreate(): Boolean {
        context?.applicationContext?.let { appCtx ->
            WebViewFactory.init(appCtx)
            initOfflineAttachmentDownloadManager(appCtx)
        }
        return true
    }

    private fun initOfflineAttachmentDownloadManager(appCtx: android.content.Context) {
        try {
            val dao = TestpressDatabase.invoke(appCtx).offlineAttachmentDao()
            val repository = OfflineAttachmentsRepository(dao)
            OfflineAttachmentDownloadManager.init(repository)
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }
    
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, 
                      selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, 
                       selectionArgs: Array<out String>?): Int = 0
}
