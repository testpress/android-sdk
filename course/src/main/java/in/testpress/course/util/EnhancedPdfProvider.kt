package `in`.testpress.course.util

import android.app.Activity
import `in`.testpress.util.BaseJavaScriptInterface
import android.webkit.JavascriptInterface
import android.util.Log
import java.io.File

class EnhancedPdfProvider(
    activity: Activity,
    private val pdfPath: String,
    private val pdfId: String?
) : BaseJavaScriptInterface(activity) {

    companion object {
        private const val TAG = "EnhancedPdfProvider"
    }

    init {
        Log.d(TAG, "Initialized with PDF path: $pdfPath, PDF ID: $pdfId")
    }

    @JavascriptInterface
    fun isPDFCached(): Boolean {
        val exists = !pdfPath.isEmpty() && File(pdfPath).exists()
        Log.d(TAG, "isPDFCached() called - Result: $exists")
        return exists
    }

    @JavascriptInterface
    fun getStreamingUrl(): String {
        return if (pdfId != null && isPDFCached()) {
            val url = "https://local.pdf/$pdfId"
            Log.d(TAG, "getStreamingUrl() called - Result: $url")
            url
        } else {
            Log.d(TAG, "getStreamingUrl() called - No PDF ID available, returning empty")
            ""
        }
    }

    @JavascriptInterface
    fun getPdfInfo(): String {
        val info = mapOf(
            "isCached" to isPDFCached(),
            "hasStreamingUrl" to (pdfId != null),
            "streamingUrl" to getStreamingUrl(),
            "fileSize" to if (isPDFCached()) File(pdfPath).length() else 0L
        )
        Log.d(TAG, "getPdfInfo() called - Result: $info")
        return info.toString()
    }
}
