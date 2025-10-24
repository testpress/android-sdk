package `in`.testpress.course.fragments

import `in`.testpress.course.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.fragments.WebViewFragment.Companion.DATA_TO_OPEN
import `in`.testpress.fragments.WebViewFragment.Companion.IS_AUTHENTICATION_REQUIRED
import `in`.testpress.fragments.WebViewFragment.Companion.SHOW_LOADING_BETWEEN_PAGES
import `in`.testpress.fragments.WebViewFragment.Companion.URL_TO_OPEN
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


class AIChatPdfFragment : Fragment() {
    
    companion object {
        private const val TAG = "AIChatPdfFragment"
        private const val ARG_CONTENT_ID = "contentId"
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_PDF_URL = "pdfUrl"
        private const val ARG_PDF_TITLE = "pdfTitle"
        
        // URLs to download LearnLens files (IIFE format - no imports, no CORS issues)
        private const val JS_URL = "https://static.testpress.in/static-staging/learnlens/learnlens-pdfchat.iife.js"
        private const val CSS_URL = "https://static.testpress.in/static-staging/learnlens/learnlens-frontend.css"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ai_pdf_fragment, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "⏱️ ================================================")
        Log.d(TAG, "⏱️ AIChatPdfFragment.onViewCreated() STARTED")
        Log.d(TAG, "⏱️ ================================================")
        
        val contentId = requireArguments().getLong(ARG_CONTENT_ID, -1L)
        val courseId = requireArguments().getLong(ARG_COURSE_ID, -1L)
        
        if (contentId == -1L || courseId == -1L) {
            throw IllegalArgumentException("Required arguments (contentId, courseId) are missing or invalid.")
        }
        
        val fragmentCreateStart = System.currentTimeMillis()
        val webViewFragment = WebViewFragment()
        Log.d(TAG, "⏱️ WebViewFragment created in: ${System.currentTimeMillis() - fragmentCreateStart}ms")
        
        // STEP 3: Load LearnLens HTML with cached JS/CSS
        val htmlGenStart = System.currentTimeMillis()
        val learnLensHtml = getLearnLensHtml(courseId, contentId)
        Log.d(TAG, "⏱️ HTML generated in: ${System.currentTimeMillis() - htmlGenStart}ms")
        
        Log.d(TAG, "📝 HTML Length: ${learnLensHtml.length} characters")
    
        val bundleStart = System.currentTimeMillis()
        webViewFragment.arguments = Bundle().apply {
            putString(DATA_TO_OPEN, learnLensHtml)  // Use DATA_TO_OPEN instead of URL_TO_OPEN
            putBoolean(IS_AUTHENTICATION_REQUIRED, false)  // No auth needed for local HTML
            putBoolean(SHOW_LOADING_BETWEEN_PAGES, false)  // Don't show loading spinner
        }
        Log.d(TAG, "⏱️ Bundle created in: ${System.currentTimeMillis() - bundleStart}ms")
        
        val transactionStart = System.currentTimeMillis()
        childFragmentManager.beginTransaction()
            .replace(R.id.aiPdf_view_fragment, webViewFragment)
            .commit()
        Log.d(TAG, "⏱️ Fragment transaction committed in: ${System.currentTimeMillis() - transactionStart}ms")
        
        val totalTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "⏱️ Total onViewCreated() time: ${totalTime}ms")
        Log.d(TAG, "⏱️ ================================================")
        
        // Download files in background for next time (if not already cached)
        val cacheDir = File(requireContext().filesDir, "learnlens_cache")
        val jsFile = File(cacheDir, "learnlens-pdfchat.iife.js")
        val cssFile = File(cacheDir, "learnlens-frontend.css")
        
        if (!jsFile.exists() || !cssFile.exists()) {
            Log.d(TAG, "📦 Files not cached, downloading for next time...")
            downloadLearnLensFiles()
        } else {
            Log.d(TAG, "✅ Files already cached at: ${cacheDir.absolutePath}")
        }
    }

    /**
     * STEP 3: Returns LearnLens HTML with cached JS/CSS
     * This mimics the Django template but loads from local files
     */
    private fun getLearnLensHtml(courseId: Long, contentId: Long): String {
        Log.d(TAG, "🎨 Creating LearnLens HTML...")
        
        // Get paths to cached files
        val cacheDir = File(requireContext().filesDir, "learnlens_cache")
        val jsFile = File(cacheDir, "learnlens-pdfchat.iife.js")
        val cssFile = File(cacheDir, "learnlens-frontend.css")
        
        // Use cached files if available, otherwise CDN
        val jsUrl = if (jsFile.exists()) {
            "file://${jsFile.absolutePath}"
        } else {
            JS_URL
        }
        
        val cssUrl = if (cssFile.exists()) {
            "file://${cssFile.absolutePath}"
        } else {
            CSS_URL
        }
        
        Log.d(TAG, "📂 JS URL: $jsUrl")
        Log.d(TAG, "📂 CSS URL: $cssUrl")
        
        // Get PDF details from arguments (passed from DocumentViewerFragment)
        val pdfUrl = requireArguments().getString(ARG_PDF_URL) 
            ?: throw IllegalArgumentException("PDF URL is required")
        val pdfTitle = requireArguments().getString(ARG_PDF_TITLE) 
            ?: "PDF Document"
        val pdfId = "content-$contentId" // Use contentId as pdfId
        val authToken = getAuthToken() // Get real auth token
        
        Log.d(TAG, "📄 PDF URL: $pdfUrl")
        Log.d(TAG, "📄 PDF Title: $pdfTitle")
        Log.d(TAG, "📄 PDF ID: $pdfId")
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>LearnLens PDF Chat</title>
                
                <!-- Load CSS from CDN -->
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
    
    private fun getAuthToken(): String {
        // TODO: Replace with real auth token when LearnLens supports it
        return "dummy-auth-token-for-testing"
    }
    
    /**
     * STEP 1: Download JS and CSS files
     * This function downloads the LearnLens files to app's internal storage
     */
    private fun downloadLearnLensFiles() {
        Log.d(TAG, "🚀 Starting download of LearnLens files...")
        
        // Create a folder to store files
        val cacheDir = File(requireContext().filesDir, "learnlens_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
            Log.d(TAG, "📁 Created folder: ${cacheDir.absolutePath}")
        }
        
        // Start download in background
        lifecycleScope.launch {
            try {
                Log.d(TAG, "📥 Downloading IIFE JavaScript file...")
                val jsFile = File(cacheDir, "learnlens-pdfchat.iife.js")
                downloadFile(JS_URL, jsFile)
                Log.d(TAG, "✅ IIFE JavaScript downloaded: ${jsFile.length() / 1024}KB")
                
                Log.d(TAG, "📥 Downloading CSS file...")
                val cssFile = File(cacheDir, "learnlens-frontend.css")
                downloadFile(CSS_URL, cssFile)
                Log.d(TAG, "✅ CSS downloaded: ${cssFile.length() / 1024}KB")
                
                Log.d(TAG, "🎉 All files downloaded successfully!")
                Log.d(TAG, "📂 Files saved to: ${cacheDir.absolutePath}")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Download failed: ${e.message}", e)
            }
        }
    }
    
    /**
     * Helper function to download a file from URL
     */
    private suspend fun downloadFile(url: String, targetFile: File) = withContext(Dispatchers.IO) {
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
