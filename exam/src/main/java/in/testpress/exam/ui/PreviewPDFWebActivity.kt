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

class JavaScriptInterface(val activity: PreviewPDFWebActivity) : BaseJavaScriptInterface(activity) {

    @JavascriptInterface
    fun openPdf(url: String, authKey: String, pdfName: String) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            val progressDialog = ProgressDialog(activity).apply {
                setMessage("Loading PDF...")
                setCancelable(false)
                show()
            }

            withContext(Dispatchers.IO) {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", authKey)
                        .build()

                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val bytes = response.body()?.bytes()
                        if (bytes != null) {
                            val file = File(activity.cacheDir, pdfName)
                            file.writeBytes(bytes)

                            withContext(Dispatchers.Main) {
                                progressDialog.dismiss()

                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(
                                        FileProvider.getUriForFile(
                                            activity,
                                            "${activity.packageName}.testpressFileProvider",
                                            file
                                        ),
                                        "application/pdf"
                                    )
                                    flags =
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                                }

                                try {
                                    activity.startActivity(intent)
                                } catch (e: ActivityNotFoundException) {
                                    showErrorDialog("No app found to open PDF.")
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                progressDialog.dismiss()
                                showErrorDialog("Download failed: PDF content was empty.")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                            showErrorDialog("Download failed with HTTP status code: ${response.code()}")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        showErrorDialog("Unexpected error occurred: ${e.localizedMessage}")
                    }
                }
            }
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