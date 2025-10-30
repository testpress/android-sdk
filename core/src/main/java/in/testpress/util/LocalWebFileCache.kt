package `in`.testpress.util

import android.content.Context
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
    
    private const val CACHE_DIR_NAME = "web_assets"
    private const val DEFAULT_MAX_AGE_HOURS = 12L
    private const val DOWNLOAD_TIMEOUT_SECONDS = 30L
    
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
            downloadIfNeeded(context, url, fileName, forceRefresh, maxAgeHours)
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
            coroutineScope {
                files.map { (url, fileName) ->
                    async { downloadIfNeeded(context, url, fileName, forceRefresh, maxAgeHours) }
                }.awaitAll()
            }
            onComplete?.invoke()
        }
    }
    
    suspend fun downloadIfNeeded(
        context: Context,
        url: String,
        fileName: String,
        forceRefresh: Boolean = false,
        maxAgeHours: Long? = DEFAULT_MAX_AGE_HOURS
    ) {
        if (shouldSkipDownload(context, fileName, forceRefresh, maxAgeHours)) return
        
        downloadMutex.withLock {
            if (shouldSkipDownload(context, fileName, forceRefresh, maxAgeHours)) return
            try {
                download(context, url, fileName)
                downloadedFiles.add(fileName)
            } catch (e: Exception) {
                // Silent fail - use fallback URL in getLocalPath
            }
        }
    }
    
    private fun shouldSkipDownload(
        context: Context,
        fileName: String,
        forceRefresh: Boolean,
        maxAgeHours: Long?
    ): Boolean {
        if (forceRefresh) return false
        if (downloadedFiles.contains(fileName)) return true
        if (!isCached(context, fileName)) return false
        return maxAgeHours == null || !isFileExpired(context, fileName, maxAgeHours)
    }
    
    private fun isCached(context: Context, fileName: String): Boolean {
        return getCacheFile(context, fileName).exists()
    }
    
    private fun isFileExpired(context: Context, fileName: String, maxAgeHours: Long): Boolean {
        val file = getCacheFile(context, fileName)
        if (!file.exists()) return true
        val fileAgeMs = System.currentTimeMillis() - file.lastModified()
        val maxAgeMs = maxAgeHours * 60 * 60 * 1000
        return fileAgeMs > maxAgeMs
    }
    
    fun getLocalPath(context: Context, fileName: String, fallbackUrl: String): String {
        val file = getCacheFile(context, fileName)
        return if (file.exists()) "file://${file.absolutePath}" else fallbackUrl
    }
    
    fun loadTemplate(context: Context, templateName: String, replacements: Map<String, String>): String {
        var html = context.assets.open("webview_templates/$templateName").bufferedReader().use { it.readText() }
        replacements.forEach { (key, value) ->
            html = html.replace("{{$key}}", value)
        }
        return html
    }
    
    fun clearAll(context: Context) {
        val cacheDir = getCacheDir(context)
        cacheDir.listFiles()?.forEach { it.delete() }
        downloadedFiles.clear()
    }
    
    private fun getCacheFile(context: Context, fileName: String): File {
        return File(getCacheDir(context), fileName)
    }
    
    private fun getCacheDir(context: Context): File {
        return File(context.filesDir, CACHE_DIR_NAME).apply { mkdirs() }
    }
    
    private suspend fun download(context: Context, url: String, fileName: String) = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        
        val request = Request.Builder().url(url).build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Download failed: ${response.code()}")
            }
            
            response.body()?.byteStream()?.use { input ->
                getCacheFile(context, fileName).outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Empty response body")
        }
    }
}
