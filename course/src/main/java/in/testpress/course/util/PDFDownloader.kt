package `in`.testpress.course.util

import `in`.testpress.course.util.FileUtils.getRootDirPath
import android.content.Context
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import java.io.File

open class PDFDownloader(
        private val pdfDownloadListener: PdfDownloadListener,
        private val context: Context,
        private val fileName: String
) {

    var file: File? = null

    init {
        file = File(getRootDirPath(context), fileName)
    }

    fun download(url: String) {
        val dirPath = getRootDirPath(context)
        PRDownloader.initialize(context)
        PRDownloader.download(url, dirPath, fileName)
                .build().start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        file = File(dirPath, fileName)
                        pdfDownloadListener.onDownloadSuccess()
                    }

                    override fun onError(error: com.downloader.Error?) {
                        pdfDownloadListener.onDownloadFailed()
                    }
                })
    }

    fun isDownloaded() {
       if (file?.isFile == true) {
           pdfDownloadListener.onDownloadSuccess()
       } else {
           pdfDownloadListener.downloadPdf()
       }
    }

    fun get(): File? {
        return file
    }
}

interface PdfDownloadListener {
    fun onDownloadSuccess()
    fun onDownloadFailed()
    fun downloadPdf()
}
