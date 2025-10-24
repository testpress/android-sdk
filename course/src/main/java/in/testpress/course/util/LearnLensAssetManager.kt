package `in`.testpress.course.util

import android.content.Context
import android.util.Log
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

/**
 * Manages LearnLens static assets (JavaScript and CSS files).
 * Downloads and caches files for offline use and faster loading.
 * 
 * Singleton pattern ensures assets are downloaded only once per app session.
 */
object LearnLensAssetManager {
    
    private const val TAG = "LearnLensAssetManager"
    
    // CDN URLs for LearnLens assets
    private const val JS_URL = "https://static.testpress.in/static-staging/learnlens/learnlens-pdfchat.iife.js"
    private const val CSS_URL = "https://static.testpress.in/static-staging/learnlens/learnlens-frontend.css"
    
    // Cache directory and file names
    private const val CACHE_DIR_NAME = "learnlens_cache"
    private const val JS_FILE_NAME = "learnlens-pdfchat.iife.js"
    private const val CSS_FILE_NAME = "learnlens-frontend.css"
    
    @Volatile
    private var isDownloaded = false
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloadMutex = Mutex()  // Thread-safe lock for coroutines
    
    /**
     * Download assets in background (non-blocking).
     * Safe to call from main thread.
     */
    fun downloadAssetsInBackground(context: Context) {
        scope.launch {
            try {
                downloadAssetsIfNeeded(context)
            } catch (e: Exception) {
                Log.e(TAG, "Background download failed: ${e.message}")
            }
        }
    }
    
    /**
     * Check if LearnLens assets are cached
     */
    fun isAssetsCached(context: Context): Boolean {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        val jsFile = File(cacheDir, JS_FILE_NAME)
        val cssFile = File(cacheDir, CSS_FILE_NAME)
        return jsFile.exists() && cssFile.exists()
    }
    
    /**
     * Download and cache LearnLens assets if not already cached.
     * Safe to call multiple times - will skip if already downloaded.
     * Thread-safe using Mutex.
     */
    suspend fun downloadAssetsIfNeeded(context: Context) {
        // Quick check without lock (optimization)
        if (isDownloaded || isAssetsCached(context)) {
            return
        }
        
        // Use Mutex to ensure only one coroutine downloads at a time
        downloadMutex.withLock {
            // Double-check after acquiring lock
            if (isDownloaded || isAssetsCached(context)) {
                return
            }
            
            try {
                downloadAssets(context)
                isDownloaded = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download LearnLens assets: ${e.message}", e)
                throw e
            }
        }
    }
    
    /**
     * Get local file path for JavaScript asset
     */
    fun getJsFilePath(context: Context): String {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        val jsFile = File(cacheDir, JS_FILE_NAME)
        return if (jsFile.exists()) {
            "file://${jsFile.absolutePath}"
        } else {
            JS_URL  // Fallback to CDN
        }
    }
    
    /**
     * Get local file path for CSS asset
     */
    fun getCssFilePath(context: Context): String {
        val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
        val cssFile = File(cacheDir, CSS_FILE_NAME)
        return if (cssFile.exists()) {
            "file://${cssFile.absolutePath}"
        } else {
            CSS_URL  // Fallback to CDN
        }
    }
    
    /**
     * Generate LearnLens HTML with embedded JavaScript and CSS
     */
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
                
                <!-- Load CSS from cached file or CDN -->
                <link rel="stylesheet" href="$cssUrl">
                
                <style>
                    body { 
                        margin: 0; 
                        padding: 0;
                        font-family: Arial, sans-serif;
                    }
                    #learnlens-pdf-chat { 
                        width: 100%; 
                        height: 100vh; 
                    }
                </style>
            </head>
            <body>
                <!-- This div will be populated by LearnLens JavaScript -->
                <div id="learnlens-pdf-chat"></div>
                
                <!-- Load IIFE JavaScript (no modules, no CORS issues) -->
                <script src="$jsUrl"></script>
                
                <!-- Initialize LearnLens after script loads -->
                <script>
                    let isInitialized = false;
                    
                    function initLearnLens() {
                        if (isInitialized) {
                            return;
                        }
                        
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
                    
                    // Try multiple times with different triggers
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
        
        // Download JavaScript
        val jsFile = File(cacheDir, JS_FILE_NAME)
        downloadFile(JS_URL, jsFile)
        
        // Download CSS
        val cssFile = File(cacheDir, CSS_FILE_NAME)
        downloadFile(CSS_URL, cssFile)
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

