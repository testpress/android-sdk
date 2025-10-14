package `in`.testpress.util.webview

import `in`.testpress.core.TestpressException
import `in`.testpress.fragments.WebViewFragment
import `in`.testpress.util.extension.openUrlInBrowser
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.regex.Pattern

class CustomWebViewClient(val fragment: WebViewFragment) : WebViewClient() {

    private var errorList = linkedMapOf<WebResourceRequest?,WebResourceResponse?>()
    
    companion object {
        private const val TAG = "CustomWebViewClient"
        private const val LOCAL_PDF_SCHEME = "https://local.pdf/"
        private val PDF_ID_PATTERN = Pattern.compile("^https://local\\.pdf/([^/]+)$")
        private val SAFE_ID_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]")
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url.toString()
        return when {
            isPDFUrl(url) -> {
                fragment.openUrlInBrowser(url)
                true
            }
            shouldLoadInWebView(url) -> false
            else -> {
                fragment.openUrlInBrowser(url)
                true
            }
        }
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
        
        Log.d(TAG, "Intercepting request: $url")
        
        // Check if this is a local PDF request
        if (!url.startsWith(LOCAL_PDF_SCHEME)) {
            return super.shouldInterceptRequest(view, request)
        }
        
        // Handle OPTIONS requests (CORS preflight)
        if (request?.method == "OPTIONS") {
            Log.d(TAG, "Handling CORS preflight request for: $url")
            return createCorsPreflightResponse()
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
        val context = fragment.requireContext()
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

    private fun isPDFUrl(url: String?) = url?.contains(".pdf") ?: false

    private fun shouldLoadInWebView(url: String?):Boolean {
        return if (fragment.isInstituteUrl(url)){
            true
        } else {
            fragment.allowNonInstituteUrlInWebView
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (fragment.showLoadingBetweenPages) fragment.showLoading()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        fragment.hideLoading()
        fragment.hideEmptyViewShowWebView()
        checkWebViewHasError()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        val requestUrl = request?.url.toString()
        val currentWebViewUrl = fragment.webView.url.toString()
        if (requestUrl == currentWebViewUrl) {
            fragment.showErrorView(TestpressException.unexpectedWebViewError(Exception("WebView error ${error?.errorCode}")))
        }
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        errorList[request] = errorResponse
    }

    private fun checkWebViewHasError() {
        // We are not showing error for other URLs like static and image URLs.
        // Because WebView can load multiple URLs simultaneously like browser.
        errorList.forEach { error ->
            val requestUrl = error.key?.url.toString()
            val currentWebViewUrl = fragment.webView.url.toString()
            if (requestUrl == currentWebViewUrl) {
                val statusCode = error.value?.statusCode ?: -1
                val reasonPhrase = error.value?.reasonPhrase ?: "Unknown Error"
                val httpError = TestpressException.httpError(statusCode, reasonPhrase)
                fragment.showErrorView(httpError)
                errorList.clear()
            }
        }
    }

    private fun createPdfResponse(pdfFile: File): WebResourceResponse {
        return try {
            val inputStream = FileInputStream(pdfFile)
            val corsHeaders = mutableMapOf<String, String>().apply {
                put("Access-Control-Allow-Origin", "*") // Allow all origins for WebView
                put("Access-Control-Allow-Methods", "GET, OPTIONS")
                put("Access-Control-Allow-Headers", "Content-Type, Range")
                put("Access-Control-Expose-Headers", "Content-Length, Accept-Ranges")
                put("Cache-Control", "private, max-age=3600")
                put("Content-Length", pdfFile.length().toString())
                put("Content-Type", "application/pdf")
            }
            
            WebResourceResponse(
                "application/pdf",
                "binary",
                200,
                "OK",
                corsHeaders,
                inputStream
            )
        } catch (e: IOException) {
            Log.e(TAG, "Error creating PDF response", e)
            createErrorResponse(500, "Internal Error")
        }
    }

    private fun createCorsPreflightResponse(): WebResourceResponse {
        val corsHeaders = mutableMapOf<String, String>().apply {
            put("Access-Control-Allow-Origin", "*")
            put("Access-Control-Allow-Methods", "GET, OPTIONS")
            put("Access-Control-Allow-Headers", "Content-Type, Range")
            put("Access-Control-Max-Age", "86400") // 24 hours
        }
        
        Log.d(TAG, "Creating CORS preflight response")
        
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            200,
            "OK",
            corsHeaders,
            "".byteInputStream()
        )
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