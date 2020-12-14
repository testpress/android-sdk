package `in`.testpress.course.util

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import java.io.File

class PDFDownloader(private val pdfDownloadListener: PdfDownloadListener) {

    var file: File? = null

    fun download(url: String, context: Context, fileName: String) {
        val dirPath = getRootDirPath(context)
        file = File(dirPath, fileName)
        if (isDownloaded(file)) {
            pdfDownloadListener.pdfDownloaded(true)
        } else {
            PRDownloader.download(url, dirPath, fileName)
                    .build().start(object : OnDownloadListener {
                        override fun onDownloadComplete() {
                            file = File(dirPath, fileName)
                            pdfDownloadListener.pdfDownloaded(true)
                        }

                        override fun onError(error: com.downloader.Error?) {
                            pdfDownloadListener.pdfDownloaded(false)
                        }
                    })
        }
    }

    private fun isDownloaded(file: File?): Boolean {
        return file?.isFile == true
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

    fun get(): File? {
        return file
    }
}

interface PdfDownloadListener {
    fun pdfDownloaded(response: Boolean)
}
