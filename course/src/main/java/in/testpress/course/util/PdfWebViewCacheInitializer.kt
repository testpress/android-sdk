package `in`.testpress.course.util

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log


class PdfWebViewCacheInitializer : ContentProvider() {
    
    companion object {
        private const val TAG = "PdfCacheInit"
    }
    
    override fun onCreate(): Boolean {
        return try {
            context?.let { ctx ->
                PdfWebViewCache.init(ctx.applicationContext)
                LocalWebFileCache.clearAll(ctx.applicationContext)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize PdfWebViewCache (non-fatal)", e)
            true
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

