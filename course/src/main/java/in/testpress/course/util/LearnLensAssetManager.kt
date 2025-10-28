package `in`.testpress.course.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object LearnLensAssetManager {
    
    private const val JS_URL = "https://static.testpress.in/static-staging/learnlens/learnlens-pdfchat.iife.js"
    private const val CSS_URL = "https://static.testpress.in/static-staging/learnlens/learnlens-frontend.css"
    private const val CACHE_DIR_NAME = "learnlens_cache"
    private const val JS_FILE_NAME = "learnlens-pdfchat.iife.js"
    private const val CSS_FILE_NAME = "learnlens-frontend.css"
    
    @Volatile
    private var isDownloaded = false
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloadMutex = Mutex()
    

    fun downloadAssetsInBackground(context: Context) {
        scope.launch {
            try {
                downloadAssetsIfNeeded(context)
            } catch (e: Exception) {
                // Silent failure - will fallback to CDN
            }
        }
    }
    

    fun isAssetsCached(context: Context): Boolean {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        val jsFile = File(cacheDir, JS_FILE_NAME)
        val cssFile = File(cacheDir, CSS_FILE_NAME)
        return jsFile.exists() && cssFile.exists()
    }
    

    suspend fun downloadAssetsIfNeeded(context: Context) {
        if (isDownloaded || isAssetsCached(context)) {
            return
        }
        
        downloadMutex.withLock {
            if (isDownloaded || isAssetsCached(context)) {
                return
            }
            
            try {
                downloadAssets(context)
                isDownloaded = true
            } catch (e: Exception) {
                throw e
            }
        }
    }
    

    fun getJsFilePath(context: Context): String {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        val jsFile = File(cacheDir, JS_FILE_NAME)
        return if (jsFile.exists()) "file://${jsFile.absolutePath}" else JS_URL
    }
    
    fun getCssFilePath(context: Context): String {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        val cssFile = File(cacheDir, CSS_FILE_NAME)
        return if (cssFile.exists()) "file://${cssFile.absolutePath}" else CSS_URL
    }
    

    fun generateLearnLensHtml(
        context: Context,
        pdfUrl: String,
        pdfTitle: String,
        pdfId: String,
        authToken: String
    ): String {
        val jsUrl = getJsFilePath(context)
        val cssUrl = getCssFilePath(context)
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>LearnLens PDF Chat</title>
                <link rel="stylesheet" href="$cssUrl">
                <style>
                    body { margin: 0; padding: 0; font-family: Arial, sans-serif; }
                    #learnlens-pdf-chat { width: 100%; height: 100vh; }
                </style>
            </head>
            <body>
                <div id="learnlens-pdf-chat"></div>
                <script src="$jsUrl"></script>
                <script>
                    let isInitialized = false;
                    
                    function initLearnLens() {
                        if (isInitialized) return;
                        
                        if (window.LearnLens && window.LearnLens.mountPdfChat) {
                            isInitialized = true;
                            window.LearnLens.mountPdfChat("learnlens-pdf-chat", {
                                pdfUrl: "$pdfUrl",
                                pdfId: "$pdfId",
                                authToken: "$authToken",
                                pdfTitle: "$pdfTitle"
                            });
                        }
                    }
                    
                    document.addEventListener('DOMContentLoaded', function() {
                        setTimeout(initLearnLens, 1000);
                    });
                    
                    window.addEventListener('load', function() {
                        setTimeout(initLearnLens, 500);
                    });
                    
                    setTimeout(initLearnLens, 3000);
                </script>
            </body>
            </html>
        """.trimIndent()
    }
    
    private suspend fun downloadAssets(context: Context) = withContext(Dispatchers.IO) {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        downloadFile(JS_URL, File(cacheDir, JS_FILE_NAME))
        downloadFile(CSS_URL, File(cacheDir, CSS_FILE_NAME))
    }
    
    private fun downloadFile(url: String, targetFile: File) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val request = Request.Builder()
            .url(url)
            .build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Download failed with code: ${response.code}")
            }
            
            response.body?.byteStream()?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Response body is null")
        }
    }
}

