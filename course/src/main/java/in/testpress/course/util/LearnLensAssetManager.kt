package `in`.testpress.course.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
     */
    suspend fun downloadAssetsIfNeeded(context: Context) {
        if (isDownloaded || isAssetsCached(context)) {
            Log.d(TAG, "✅ Assets already cached")
            return
        }
        
        synchronized(this) {
            if (isDownloaded || isAssetsCached(context)) {
                return
            }
            
            try {
                Log.d(TAG, "📦 Downloading LearnLens assets...")
                downloadAssets(context)
                isDownloaded = true
                Log.d(TAG, "✅ Assets downloaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to download assets: ${e.message}", e)
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
                    const pageLoadStart = performance.now();
                    let isInitialized = false;  // Prevent multiple initializations
                    
                    console.log('⏱️ Page load started at:', pageLoadStart);
                    console.log('✅ Loading JS from CDN: $jsUrl');
                    
                    // Function to initialize LearnLens
                    function initLearnLens() {
                        if (isInitialized) {
                            console.log('⚠️ Already initialized, skipping...');
                            return;
                        }
                        
                        const checkTime = performance.now();
                        console.log('🔄 Checking for LearnLens at:', checkTime, 'ms');
                        console.log('   window.LearnLens:', window.LearnLens);
                        console.log('   typeof window.LearnLens:', typeof window.LearnLens);
                        
                        if (window.LearnLens && window.LearnLens.mountPdfChat) {
                            isInitialized = true;  // Mark as initialized
                            
                            const initStart = performance.now();
                            console.log('✅ LearnLens found! Initializing at:', initStart, 'ms');
                            console.log('⏱️ Time to load LearnLens:', (initStart - pageLoadStart).toFixed(2), 'ms');
                            
                            window.LearnLens.mountPdfChat("learnlens-pdf-chat", {
                                pdfUrl: "$pdfUrl",
                                pdfId: "$pdfId",
                                authToken: "$authToken",
                                pdfTitle: "$pdfTitle"
                            });
                            
                            const initEnd = performance.now();
                            console.log('✅ LearnLens initialized!');
                            console.log('⏱️ Total time from page load:', (initEnd - pageLoadStart).toFixed(2), 'ms');
                            console.log('⏱️ Initialization took:', (initEnd - initStart).toFixed(2), 'ms');
                        } else {
                            console.warn('⚠️ LearnLens not ready yet. Will retry...');
                            console.log('Available on window:', Object.keys(window).slice(0, 10));
                        }
                    }
                    
                    // Try multiple times with different triggers
                    document.addEventListener('DOMContentLoaded', function() {
                        console.log('✅ DOMContentLoaded fired at:', performance.now(), 'ms');
                        setTimeout(initLearnLens, 1000);
                    });
                    
                    window.addEventListener('load', function() {
                        console.log('✅ Window load event fired at:', performance.now(), 'ms');
                        setTimeout(initLearnLens, 500);
                    });
                    
                    // Also try immediately after a delay
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
        Log.d(TAG, "📥 JavaScript: ${jsFile.length() / 1024}KB")
        
        // Download CSS
        val cssFile = File(cacheDir, CSS_FILE_NAME)
        downloadFile(CSS_URL, cssFile)
        Log.d(TAG, "📥 CSS: ${cssFile.length() / 1024}KB")
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

