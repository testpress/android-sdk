package `in`.testpress.course.util

import android.app.Activity
import `in`.testpress.util.BaseJavaScriptInterface
import android.webkit.JavascriptInterface
import java.io.File
import java.io.FileInputStream
import android.util.Base64

class CachedPdfPathProvider(
    activity: Activity,
    private val pdfPath: String
) : BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun isPDFCached(): Boolean {
        return !pdfPath.isEmpty() && File(pdfPath).exists()
    }

    @JavascriptInterface
    fun getBase64PdfData(): String {
        return if (isPDFCached()) {
            try {
                val file = File(pdfPath)
                val inputStream = FileInputStream(file)
                val bytes = inputStream.readBytes()
                inputStream.close()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
    }

    @JavascriptInterface
    fun getBase64PdfDataUrl(): String {
        val base64Data = getBase64PdfData()
        return if (base64Data.isNotEmpty()) {
            "data:application/pdf;base64,$base64Data"
        } else {
            ""
        }
    }
}
