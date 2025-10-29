package `in`.testpress.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object LocalWebFileCache {
    private const val TAG = "LocalWebFileCache"
    
    private const val CACHE_DIR_NAME = "web_assets"
    private const val DEFAULT_MAX_AGE_HOURS = 12L
    
    @Volatile
    private var downloadedFiles = mutableSetOf<String>()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloadMutex = Mutex()
    
    fun downloadInBackground(
        context: Context, 
        url: String, 
        fileName: String, 
        forceRefresh: Boolean = false,
        maxAgeHours: Long? = DEFAULT_MAX_AGE_HOURS
    ) {
        scope.launch {
            try {
                downloadIfNeeded(context, url, fileName, forceRefresh, maxAgeHours)
            } catch (e: Exception) {
            }
        }
    }
    
    fun downloadMultipleInBackground(
        context: Context,
        files: List<Pair<String, String>>,
        forceRefresh: Boolean = false,
        maxAgeHours: Long? = DEFAULT_MAX_AGE_HOURS,
        onComplete: (() -> Unit)? = null
    ) {
        scope.launch {
            try {
                coroutineScope {
                    files.map { (url, fileName) ->
                        async {
                            downloadIfNeeded(context, url, fileName, forceRefresh, maxAgeHours)
                        }
                    }.awaitAll()
                }
                onComplete?.invoke()
            } catch (e: Exception) {
            }
        }
    }
    
    fun isCached(context: Context, fileName: String): Boolean {
        val file = File(File(context.filesDir, CACHE_DIR_NAME), fileName)
        return file.exists()
    }
    
    suspend fun downloadIfNeeded(
        context: Context, 
        url: String, 
        fileName: String,
        forceRefresh: Boolean = false,
        maxAgeHours: Long? = DEFAULT_MAX_AGE_HOURS
    ) {
        if (!forceRefresh && downloadedFiles.contains(fileName)) {
            Log.d(TAG, "File already downloaded in session: $fileName")
            return
        }
        
        if (!forceRefresh && isCached(context, fileName)) {
            if (maxAgeHours == null || !isFileExpired(context, fileName, maxAgeHours)) {
                Log.d(TAG, "✓ Cache HIT: $fileName")
                return
            } else {
                Log.d(TAG, "⚠ Cache EXPIRED: $fileName (max age: ${maxAgeHours}h)")
            }
        }
        
        downloadMutex.withLock {
            if (!forceRefresh && downloadedFiles.contains(fileName)) {
                return
            }
            
            if (!forceRefresh && isCached(context, fileName)) {
                if (maxAgeHours == null || !isFileExpired(context, fileName, maxAgeHours)) {
                    Log.d(TAG, "✓ Cache HIT (after lock): $fileName")
                    return
                }
            }
            
            try {
                Log.d(TAG, "⬇ Downloading: $fileName from $url")
                val startTime = System.currentTimeMillis()
                download(context, url, fileName)
                downloadedFiles.add(fileName)
                val duration = System.currentTimeMillis() - startTime
                Log.d(TAG, "✓ Downloaded: $fileName (${duration}ms)")
            } catch (e: Exception) {
                throw e
            }
        }
    }
    
    private fun isFileExpired(context: Context, fileName: String, maxAgeHours: Long): Boolean {
        val file = File(File(context.filesDir, CACHE_DIR_NAME), fileName)
        if (!file.exists()) return true
        
        val fileAge = System.currentTimeMillis() - file.lastModified()
        val maxAgeMillis = maxAgeHours * 60 * 60 * 1000
        return fileAge > maxAgeMillis
    }
    
    fun getLocalPath(context: Context, fileName: String, fallbackUrl: String): String {
        val file = File(File(context.filesDir, CACHE_DIR_NAME), fileName)
        return if (file.exists()) {
            Log.d(TAG, "→ Using local: $fileName")
            "file://${file.absolutePath}"
        } else {
            Log.d(TAG, "→ Using remote: $fallbackUrl")
            fallbackUrl
        }
    }
    
    fun clearAll(context: Context) {
        try {
            val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
            val fileCount = cacheDir.listFiles()?.size ?: 0
            cacheDir.deleteRecursively()
            downloadedFiles.clear()
            Log.d(TAG, "🗑 Cleared cache: $fileCount files deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
        }
    }
    
    fun loadTemplate(context: Context, templateName: String, replacements: Map<String, String>): String {
        var html = context.assets.open("webview_templates/$templateName").bufferedReader().use { it.readText() }
        replacements.forEach { (key, value) ->
            html = html.replace("{{$key}}", value)
        }
        return html
    }
    
    private suspend fun download(context: Context, url: String, fileName: String) = withContext(Dispatchers.IO) {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val request = Request.Builder().url(url).build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Download failed with code: ${response.code()}")
            }
            
            response.body()?.byteStream()?.use { input ->
                File(cacheDir, fileName).outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Response body is null")
        }
    }
}

