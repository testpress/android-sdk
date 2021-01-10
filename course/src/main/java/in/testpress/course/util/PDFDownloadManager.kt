package `in`.testpress.course.util

import `in`.testpress.course.util.FileUtils.getRootDirPath
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.request.DownloadRequest
import java.io.File

open class PDFDownloadManager(
        private val pdfDownloadListener: PdfDownloadListener,
        private val context: Context,
        private val fileName: String
) {

    var file: File? = null

    private lateinit var fileEncryptAndDecryptUtil: FileEncryptAndDecryptUtil

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var prDownloader: DownloadRequest

    init {
        file = File(getRootDirPath(context), fileName)
        if (isDownloaded()) { fileEncryptAndDecryptUtil = FileEncryptAndDecryptUtil(file!!) }
    }

    fun download(url: String) {
        val dirPath = getRootDirPath(context)
        PRDownloader.initialize(context)

        prDownloader = PRDownloader.download(url, dirPath, fileName).build()
        prDownloader.start(object : OnDownloadListener {
            override fun onDownloadComplete() {
                file = File(dirPath, fileName)
                fileEncryptAndDecryptUtil = FileEncryptAndDecryptUtil(file!!)
                file?.let { fileEncryptAndDecryptUtil.encrypt() }
                pdfDownloadListener.onDownloadSuccess()
            }

            override fun onError(error: Error?) {
                pdfDownloadListener.onDownloadFailed()
            }
        })

        prDownloader.setOnProgressListener {
            val progress = ((it.currentBytes*100)/it.totalBytes)
            pdfDownloadListener.downloadProgress(progress.toInt())
        }
    }

    fun cancel() {
        if (::prDownloader.isInitialized) {
            prDownloader.cancel()
        }
    }

    fun cleanup() {
        if (::fileEncryptAndDecryptUtil.isInitialized) {
            fileEncryptAndDecryptUtil.cleanup()
        }
    }

    fun isDownloaded(): Boolean {
       return file?.isFile == true
    }

    fun get(): File {
        return fileEncryptAndDecryptUtil.decrypt()
    }
}

interface PdfDownloadListener {
    fun onDownloadSuccess()
    fun onDownloadFailed()
    fun downloadProgress(progress: Int)
}
