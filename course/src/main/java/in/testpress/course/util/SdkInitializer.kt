package `in`.testpress.course.util

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 * SDK auto-initializer using ContentProvider
 * - Initializes WebViewFactory with dynamic memory management
 * - Runs before Application.onCreate()
 * - No user action required
 */
class SdkInitializer : ContentProvider() {
    
    companion object {
        private const val TAG = "SdkInitializer"
    }
    
    override fun onCreate(): Boolean {
        return try {
            context?.let { ctx ->
                WebViewFactory.init(ctx.applicationContext)
                Log.d(TAG, "✓ SDK components initialized")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SDK components (non-fatal)", e)
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

