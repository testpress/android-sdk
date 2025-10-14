package `in`.testpress.course.util

import android.app.Activity
import `in`.testpress.util.BaseJavaScriptInterface
import android.webkit.JavascriptInterface
import java.io.File
import java.io.FileInputStream
import android.util.Base64
import android.util.Log

class CachedPdfPathProvider(
    activity: Activity,
    private val pdfPath: String
) : BaseJavaScriptInterface(activity) {

    init {
        Log.d("CachedPdfPathProvider", "Initialized with PDF path: $pdfPath")
        Log.d("CachedPdfPathProvider", "PDF file exists: ${File(pdfPath).exists()}")
        Log.d("CachedPdfPathProvider", "PDF file size: ${if (File(pdfPath).exists()) File(pdfPath).length() else "N/A"} bytes")
    }

    @JavascriptInterface
    fun isPDFCached(): Boolean {
        val exists = !pdfPath.isEmpty() && File(pdfPath).exists()
        Log.d("CachedPdfPathProvider", "isPDFCached() called - Result: $exists")
        return exists
    }

    @JavascriptInterface
    fun getBase64PdfData(): String {
        Log.d("CachedPdfPathProvider", "getBase64PdfData() called")
        return if (isPDFCached()) {
            try {
                val file = File(pdfPath)
                val inputStream = FileInputStream(file)
                val bytes = inputStream.readBytes()
                inputStream.close()
                val base64Data = Base64.encodeToString(bytes, Base64.DEFAULT)
                Log.d("CachedPdfPathProvider", "Base64 data length: ${base64Data.length} characters")
                Log.d("CachedPdfPathProvider", "Base64 data preview: ${base64Data.take(100)}...")
                return base64Data
            } catch (e: Exception) {
                Log.e("CachedPdfPathProvider", "Error reading PDF file", e)
                ""
            }
        } else {
            Log.d("CachedPdfPathProvider", "PDF not cached, returning empty string")
            ""
        }
    }

    @JavascriptInterface
    fun getBase64PdfDataUrl(): String {
        Log.d("CachedPdfPathProvider", "getBase64PdfDataUrl() called")
        val base64Data = getBase64PdfData()
        val dataUrl = if (base64Data.isNotEmpty()) {
            "data:application/pdf;base64,$base64Data"
        } else {
            ""
        }
        Log.d("CachedPdfPathProvider", "Data URL length: ${dataUrl.length} characters")
        Log.d("CachedPdfPathProvider", "Data URL preview: ${dataUrl.take(150)}...")
        return dataUrl
    }
}
