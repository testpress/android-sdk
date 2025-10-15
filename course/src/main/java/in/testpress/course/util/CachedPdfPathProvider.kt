package `in`.testpress.course.util

import android.app.Activity
import android.content.Context
import `in`.testpress.util.BaseJavaScriptInterface
import android.webkit.JavascriptInterface
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class CachedPdfPathProvider(
    activity: Activity,
    private val pdfPath: String
) : BaseJavaScriptInterface(activity) {

    companion object {
        private const val TAG = "CachedPdfPathProvider"
        private const val BASE64_CACHE_DIR = "pdf-base64-cache"
        // Conservative limit: 30MB max for Base64 cache (~5-6 PDFs)
        // Leaves plenty of space for other app modules (images, videos, API cache, etc.)
        private const val MAX_CACHE_SIZE_MB = 30
        private const val MAX_CACHE_SIZE_BYTES = MAX_CACHE_SIZE_MB * 1024 * 1024L
        // Maximum percentage of total app cache we're allowed to use (10%)
        private const val MAX_CACHE_PERCENTAGE = 0.10f
    }

    private val context: Context = activity.applicationContext

    @JavascriptInterface
    fun isPDFCached(): Boolean {
        val exists = !pdfPath.isEmpty() && File(pdfPath).exists()
        Log.d(TAG, "isPDFCached() - path: $pdfPath, exists: $exists")
        return exists
    }

    @JavascriptInterface
    fun getBase64PdfData(): String {
        Log.d(TAG, "========================================")
        Log.d(TAG, "getBase64PdfData() called")
        Log.d(TAG, "PDF Path: $pdfPath")
        
        if (!isPDFCached()) {
            Log.w(TAG, "❌ PDF not cached, returning empty")
            Log.d(TAG, "========================================")
            return ""
        }

        return try {
            val pdfFile = File(pdfPath)
            Log.d(TAG, "✓ PDF file exists: ${pdfFile.absolutePath}")
            Log.d(TAG, "✓ PDF size: ${pdfFile.length() / 1024}KB (${pdfFile.length()} bytes)")
            
            val base64CacheFile = getBase64CacheFile(pdfFile)
            Log.d(TAG, "Base64 cache file path: ${base64CacheFile.absolutePath}")

            // Check if Base64 cache exists and is valid
            if (base64CacheFile.exists() && isBase64CacheValid(pdfFile, base64CacheFile)) {
                Log.d(TAG, "✓ Base64 cache EXISTS and is VALID")
                Log.d(TAG, "✓ Cache size: ${base64CacheFile.length() / 1024}KB")
                Log.d(TAG, "⚡ Reading from cache (FAST PATH)")
                
                val startTime = System.currentTimeMillis()
                val cachedData = base64CacheFile.readText()
                val duration = System.currentTimeMillis() - startTime
                
                Log.d(TAG, "✓ Read ${cachedData.length} chars from cache in ${duration}ms")
                Log.d(TAG, "========================================")
                cachedData
            } else {
                if (!base64CacheFile.exists()) {
                    Log.d(TAG, "⚠ Base64 cache does NOT exist")
                } else {
                    Log.d(TAG, "⚠ Base64 cache is STALE (PDF modified)")
                }
                
                Log.d(TAG, "🔄 Encoding PDF to Base64 (SLOW PATH)...")
                val base64Data = encodePdfToBase64(pdfFile)
                
                Log.d(TAG, "✓ Encoded to ${base64Data.length} chars")
                
                // Save to cache if within size limits
                if (base64Data.isNotEmpty()) {
                    saveBase64ToCache(base64CacheFile, base64Data)
                } else {
                    Log.w(TAG, "⚠ Empty Base64 data, not caching")
                }
                
                Log.d(TAG, "========================================")
                base64Data
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR getting Base64 PDF data", e)
            Log.e(TAG, "Exception: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            Log.d(TAG, "========================================")
            ""
        }
    }

    @JavascriptInterface
    fun getBase64PdfDataUrl(): String {
        Log.d(TAG, "getBase64PdfDataUrl() called")
        val base64Data = getBase64PdfData()
        return if (base64Data.isNotEmpty()) {
            val dataUrl = "data:application/pdf;base64,$base64Data"
            Log.d(TAG, "✓ Returning data URL (${dataUrl.length} chars)")
            dataUrl
        } else {
            Log.w(TAG, "⚠ Empty Base64 data, returning empty data URL")
            ""
        }
    }

    private fun getBase64CacheFile(pdfFile: File): File {
        val cacheDir = File(context.cacheDir, BASE64_CACHE_DIR)
        if (!cacheDir.exists()) {
            Log.d(TAG, "Creating Base64 cache directory: ${cacheDir.absolutePath}")
            cacheDir.mkdirs()
        }
        
        // Use hash of PDF path as cache filename to avoid collisions
        val hash = hashString(pdfFile.absolutePath)
        Log.d(TAG, "PDF path hash: $hash")
        return File(cacheDir, "$hash.base64")
    }

    private fun isBase64CacheValid(pdfFile: File, cacheFile: File): Boolean {
        // Cache is valid if it exists and PDF hasn't been modified since cache creation
        val isValid = cacheFile.exists() && cacheFile.lastModified() >= pdfFile.lastModified()
        
        Log.d(TAG, "Cache validation:")
        Log.d(TAG, "  - Cache exists: ${cacheFile.exists()}")
        if (cacheFile.exists()) {
            Log.d(TAG, "  - Cache modified: ${cacheFile.lastModified()}")
            Log.d(TAG, "  - PDF modified: ${pdfFile.lastModified()}")
            Log.d(TAG, "  - Is valid: $isValid")
        }
        
        return isValid
    }

    private fun encodePdfToBase64(pdfFile: File): String {
        Log.d(TAG, "🔄 Starting Base64 encoding...")
        Log.d(TAG, "  - Input file: ${pdfFile.absolutePath}")
        Log.d(TAG, "  - File size: ${pdfFile.length() / 1024}KB (${pdfFile.length()} bytes)")
        
        val startTime = System.currentTimeMillis()
        val bytes = FileInputStream(pdfFile).use { it.readBytes() }
        Log.d(TAG, "  - Read ${bytes.size} bytes from file")
        
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val duration = System.currentTimeMillis() - startTime
        
        Log.d(TAG, "✓ Encoding complete!")
        Log.d(TAG, "  - Input: ${pdfFile.length()} bytes")
        Log.d(TAG, "  - Output: ${base64.length} chars")
        Log.d(TAG, "  - Expansion: ${String.format("%.2f", base64.length.toFloat() / pdfFile.length())}x")
        Log.d(TAG, "  - Duration: ${duration}ms")
        
        return base64
    }

    private fun saveBase64ToCache(cacheFile: File, base64Data: String) {
        Log.d(TAG, "💾 Saving Base64 to cache...")
        try {
            val cacheDir = cacheFile.parentFile ?: return
            
            // Get effective cache limit (respects total app cache usage)
            val effectiveCacheLimit = getEffectiveCacheLimit()
            
            // Check total cache size before saving
            val currentCacheSize = getCacheDirSize(cacheDir)
            val newFileSize = base64Data.length.toLong()
            
            Log.d(TAG, "Cache statistics:")
            Log.d(TAG, "  - Current Base64 cache size: ${currentCacheSize / 1024}KB")
            Log.d(TAG, "  - New file size: ${newFileSize / 1024}KB")
            Log.d(TAG, "  - Total after save: ${(currentCacheSize + newFileSize) / 1024}KB")
            Log.d(TAG, "  - Effective cache limit: ${effectiveCacheLimit / 1024}KB")
            
            if (currentCacheSize + newFileSize > effectiveCacheLimit) {
                Log.w(TAG, "⚠ Cache size limit reached!")
                Log.w(TAG, "  - Need to free: ${((currentCacheSize + newFileSize - effectiveCacheLimit) / 1024)}KB")
                cleanOldCacheFiles(cacheDir, newFileSize, effectiveCacheLimit)
            } else {
                Log.d(TAG, "✓ Cache size within limits")
            }
            
            val startTime = System.currentTimeMillis()
            cacheFile.writeText(base64Data)
            val duration = System.currentTimeMillis() - startTime
            
            Log.d(TAG, "✓ Saved Base64 cache successfully!")
            Log.d(TAG, "  - File: ${cacheFile.absolutePath}")
            Log.d(TAG, "  - Size: ${newFileSize / 1024}KB")
            Log.d(TAG, "  - Write time: ${duration}ms")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving Base64 cache", e)
            Log.e(TAG, "Exception: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun getCacheDirSize(dir: File): Long {
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    private fun getEffectiveCacheLimit(): Long {
        // Get total app cache size
        val totalCacheSize = getTotalCacheSize(context.cacheDir)
        
        // Calculate 10% of total cache, but cap at our MAX_CACHE_SIZE_BYTES
        val percentageBasedLimit = (totalCacheSize * MAX_CACHE_PERCENTAGE).toLong()
        val effectiveLimit = minOf(percentageBasedLimit, MAX_CACHE_SIZE_BYTES)
        
        Log.d(TAG, "Cache limit calculation:")
        Log.d(TAG, "  - Total app cache: ${totalCacheSize / (1024 * 1024)}MB")
        Log.d(TAG, "  - 10% of total: ${percentageBasedLimit / (1024 * 1024)}MB")
        Log.d(TAG, "  - Hard limit: ${MAX_CACHE_SIZE_BYTES / (1024 * 1024)}MB")
        Log.d(TAG, "  - Effective limit: ${effectiveLimit / (1024 * 1024)}MB")
        
        return effectiveLimit
    }

    private fun getTotalCacheSize(cacheDir: File): Long {
        var totalSize = 0L
        cacheDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                totalSize += file.length()
            }
        }
        return totalSize
    }

    private fun cleanOldCacheFiles(cacheDir: File, requiredSpace: Long, cacheLimit: Long) {
        Log.d(TAG, "🗑️ Starting LRU cache cleanup...")
        
        val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return
        Log.d(TAG, "  - Total files in cache: ${files.size}")
        
        var freedSpace = 0L
        var deletedCount = 0
        
        for (file in files) {
            val currentSize = getCacheDirSize(cacheDir)
            if (currentSize + requiredSpace <= cacheLimit) {
                Log.d(TAG, "✓ Enough space freed, stopping cleanup")
                break
            }
            
            val fileSize = file.length()
            Log.d(TAG, "  - Deleting: ${file.name} (${fileSize / 1024}KB, modified: ${file.lastModified()})")
            
            if (file.delete()) {
                freedSpace += fileSize
                deletedCount++
                Log.d(TAG, "    ✓ Deleted successfully")
            } else {
                Log.w(TAG, "    ✗ Failed to delete")
            }
        }
        
        Log.d(TAG, "🗑️ Cleanup complete!")
        Log.d(TAG, "  - Files deleted: $deletedCount")
        Log.d(TAG, "  - Space freed: ${freedSpace / 1024}KB")
        Log.d(TAG, "  - New cache size: ${getCacheDirSize(cacheDir) / 1024}KB")
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
