package `in`.testpress.course.util

import android.content.Context
import android.util.Log
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class PdfCacheManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "PdfCacheManager"
        private const val CACHE_DIR_NAME = "pdf-cache"
        
        @Volatile
        private var INSTANCE: PdfCacheManager? = null
        
        fun getInstance(context: Context): PdfCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PdfCacheManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val pdfIdToPathMap = ConcurrentHashMap<String, String>()
    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * Register a PDF file with a unique ID for local streaming
     * @param pdfPath The actual file path of the PDF
     * @return A unique PDF ID that can be used in https://local.pdf/{pdfId} URLs
     */
    fun registerPdf(pdfPath: String): String {
        val pdfFile = File(pdfPath)
        if (!pdfFile.exists() || !pdfFile.isFile) {
            throw IllegalArgumentException("PDF file does not exist: $pdfPath")
        }
        
        // Generate a unique ID based on file path and modification time
        val uniqueId = generateUniqueId(pdfPath, pdfFile.lastModified())
        
        // Copy file to cache directory if not already there
        val cachedFile = File(cacheDir, "$uniqueId.pdf")
        if (!cachedFile.exists()) {
            try {
                pdfFile.copyTo(cachedFile, overwrite = true)
                Log.d(TAG, "Cached PDF: $pdfPath -> ${cachedFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cache PDF: $pdfPath", e)
                throw RuntimeException("Failed to cache PDF file", e)
            }
        }
        
        pdfIdToPathMap[uniqueId] = cachedFile.absolutePath
        Log.d(TAG, "Registered PDF with ID: $uniqueId")
        
        return uniqueId
    }
    
    /**
     * Get the local PDF URL for a registered PDF ID
     */
    fun getLocalPdfUrl(pdfId: String): String {
        return "https://local.pdf/$pdfId"
    }
    
    /**
     * Check if a PDF ID is registered
     */
    fun isPdfRegistered(pdfId: String): Boolean {
        return pdfIdToPathMap.containsKey(pdfId)
    }
    
    /**
     * Get the cached file path for a PDF ID
     */
    fun getCachedFilePath(pdfId: String): String? {
        return pdfIdToPathMap[pdfId]
    }
    
    /**
     * Unregister a PDF ID (cleanup)
     */
    fun unregisterPdf(pdfId: String) {
        val cachedPath = pdfIdToPathMap.remove(pdfId)
        if (cachedPath != null) {
            try {
                File(cachedPath).delete()
                Log.d(TAG, "Unregistered and deleted PDF: $pdfId")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete cached PDF: $cachedPath", e)
            }
        }
    }
    
    /**
     * Clear all cached PDFs
     */
    fun clearCache() {
        try {
            cacheDir.listFiles()?.forEach { it.delete() }
            pdfIdToPathMap.clear()
            Log.d(TAG, "Cleared PDF cache")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear PDF cache", e)
        }
    }
    
    private fun generateUniqueId(pdfPath: String, lastModified: Long): String {
        val input = "$pdfPath:$lastModified"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }.take(16)
    }
}
