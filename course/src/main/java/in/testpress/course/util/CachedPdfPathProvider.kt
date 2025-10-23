package `in`.testpress.course.util

import android.app.Activity
import `in`.testpress.util.BaseJavaScriptInterface
import android.webkit.JavascriptInterface
import java.io.File

class CachedPdfPathProvider(
    activity: Activity,
    private val pdfPath: String
) : BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun getCachedPDFPath(): String {
        if (isPDFCached()) return "file://$pdfPath" else return ""
    }

    @JavascriptInterface
    fun isPDFCached(): Boolean {
        return !pdfPath.isEmpty() && File(pdfPath).exists()
    }
}
