package `in`.testpress.course.util

import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.util.webview.CustomWebViewClient
import android.content.Context
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.regex.Pattern

class PdfWebViewClient(
    fragment: WebViewFragment,
    private val context: Context
) : CustomWebViewClient(fragment) {

    companion object {
        private const val TAG = "PdfWebViewClient"
        private const val LOCAL_PDF_SCHEME = "https://local.pdf/"
        private val PDF_ID_PATTERN = Pattern.compile("^https://local\\.pdf/([^/]+)$")
        private val SAFE_ID_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]")
    }

    override fun shouldInterceptRequest(
        view: android.webkit.WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
        
        Log.d(TAG, "Intercepting request: $url")
        
        // Check if this is a local PDF request
        if (!url.startsWith(LOCAL_PDF_SCHEME)) {
            return super.shouldInterceptRequest(view, request)
        }
        
        // Extract PDF ID from URL
        val matcher = PDF_ID_PATTERN.matcher(url)
        if (!matcher.matches()) {
            Log.w(TAG, "Invalid local PDF URL format: $url")
            return createErrorResponse(400, "Bad Request")
        }
        
        val pdfId = matcher.group(1)
        Log.d(TAG, "Extracted PDF ID: $pdfId")
        
        // Sanitize PDF ID to prevent path traversal
        val safeId = SAFE_ID_PATTERN.matcher(pdfId).replaceAll("_")
        Log.d(TAG, "Sanitized PDF ID: $safeId")
        
        // Resolve cached file location
        val cacheDir = File(context.cacheDir, "pdf-cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        val pdfFile = File(cacheDir, "$safeId.pdf")
        Log.d(TAG, "Looking for PDF file: ${pdfFile.absolutePath}")
        
        return try {
            if (pdfFile.exists() && pdfFile.isFile) {
                Log.d(TAG, "Serving PDF file: ${pdfFile.absolutePath} (${pdfFile.length()} bytes)")
                createPdfResponse(pdfFile)
            } else {
                Log.w(TAG, "PDF file not found: ${pdfFile.absolutePath}")
                createErrorResponse(404, "Not Found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error serving PDF file", e)
            createErrorResponse(500, "Internal Error")
        }
    }

    private fun createPdfResponse(pdfFile: File): WebResourceResponse {
        return try {
            val inputStream = FileInputStream(pdfFile)
            val headers = mapOf(
                "Cache-Control" to "private, max-age=3600",
                "Content-Length" to pdfFile.length().toString(),
                "Content-Type" to "application/pdf"
            )
            
            WebResourceResponse(
                "application/pdf",
                "binary",
                200,
                "OK",
                headers,
                inputStream
            )
        } catch (e: IOException) {
            Log.e(TAG, "Error creating PDF response", e)
            createErrorResponse(500, "Internal Error")
        }
    }

    private fun createErrorResponse(status: Int, message: String): WebResourceResponse {
        val statusText = when (status) {
            400 -> "Bad Request"
            404 -> "Not Found"
            500 -> "Internal Server Error"
            else -> "Error"
        }
        
        Log.d(TAG, "Creating error response: $status $statusText - $message")
        
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            status,
            statusText,
            mapOf("Content-Type" to "text/plain"),
            message.byteInputStream()
        )
    }
}
