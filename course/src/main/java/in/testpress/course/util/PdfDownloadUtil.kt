package `in`.testpress.course.util

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import java.io.File

class PdfDownloadUtil(private val pdfDownloadListener: PdfDownloadListener) {

    var file: File? = null

    fun downloadPdfFromInternet(url: String, context: Context, fileName: String) {
        val dirPath = getRootDirPath(context)
        file = File(dirPath, fileName)
        if (file?.isFile == true) {
            pdfDownloadListener.isPdfDownloaded(true, file)
        } else {
            PRDownloader.download(url, dirPath, fileName)
                    .build().start(object : OnDownloadListener {
                        override fun onDownloadComplete() {
                            file = File(dirPath, fileName)
                            pdfDownloadListener.isPdfDownloaded(true, file)
                        }

                        override fun onError(error: com.downloader.Error?) {
                            pdfDownloadListener.isPdfDownloaded(false, null)
                        }
                    })
        }
    }

    private fun getRootDirPath(context: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file: File = ContextCompat.getExternalFilesDirs(
                    context.applicationContext,
                    null
            )[0]
            file.absolutePath
        } else {
           context.applicationContext.filesDir.absolutePath
        }
    }
}

interface PdfDownloadListener {
    fun isPdfDownloaded(response: Boolean, file: File?)
}
