package `in`.testpress.exam.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.webkit.JavascriptInterface
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import `in`.testpress.ui.AbstractWebViewActivity
import `in`.testpress.util.BaseJavaScriptInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class PreviewPDFWebActivity : AbstractWebViewActivity() {
    override fun onWebViewInitializationSuccess() {
        webViewFragment.addJavascriptInterface(JavaScriptInterface(this), "AndroidInterface")
    }
}

class JavaScriptInterface(private val activity: PreviewPDFWebActivity) :
    BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun openPdf(url: String, authKey: String, pdfName: String) {
        when {
            url.isBlank() -> showErrorDialog("URL is missing")
            authKey.isBlank() -> showErrorDialog("Auth Key is missing")
            else -> return
        }

        activity.lifecycleScope.launch(Dispatchers.Main) {
            val progressDialog = showLoadingDialog()
            val fileName = pdfName.ifBlank { "response.pdf" }
            downloadAndOpenPdf(url, authKey, fileName, progressDialog)
        }
    }

    private suspend fun downloadAndOpenPdf(
        url: String,
        authKey: String,
        pdfName: String,
        progressDialog: ProgressDialog
    ) = withContext(Dispatchers.IO) {
        try {
            fetchPdf(url, authKey).use { response ->
                if (!response.isSuccessful) {
                    onDownloadFailed("Download failed with HTTP status code: ${response.code()}", progressDialog)
                    return@withContext
                }

                val file = savePdfToFile(response.body()?.bytes(), pdfName)
                if (file == null) {
                    onDownloadFailed("Download failed: PDF content was empty.", progressDialog)
                    return@withContext
                }

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    openPdfFile(file)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                showErrorDialog("Failed to download PDF: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    private fun fetchPdf(url: String, authKey: String) =
        OkHttpClient().newCall(
            Request.Builder()
                .url(url)
                .addHeader("Authorization", authKey)
                .build()
        ).execute()

    private fun savePdfToFile(bytes: ByteArray?, fileName: String): File? {
        if (bytes == null) return null
        return try {
            val file = File(activity.cacheDir, fileName)
            file.writeBytes(bytes)
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun openPdfFile(file: File) {
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.testpressFileProvider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
        }

        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showErrorDialog("No app found to open PDF.")
        }
    }

    private fun onDownloadFailed(message: String, progressDialog: ProgressDialog) {
        activity.runOnUiThread {
            progressDialog.dismiss()
            showErrorDialog(message)
        }
    }

    private fun showLoadingDialog(): ProgressDialog {
        return ProgressDialog(activity).apply {
            setMessage("Loading PDF...")
            setCancelable(false)
            show()
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(activity).apply {
            setTitle("Error")
            setMessage(message)
            setCancelable(true)
            setPositiveButton("OK", null)
            show()
        }
    }
}