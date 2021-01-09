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

    private var fileEncryptionAndDecryption = FileEncryptionAndDecryption()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var prDownloader: DownloadRequest

    init {
        file = File(getRootDirPath(context), fileName)
    }

    fun download(url: String) {
        val dirPath = getRootDirPath(context)
        PRDownloader.initialize(context)

        prDownloader = PRDownloader.download(url, dirPath, fileName).build()
        prDownloader.start(object : OnDownloadListener {
            override fun onDownloadComplete() {
                file = File(dirPath, fileName)
                file?.let { FileEncryptionAndDecryption.encrypt(it) }
                pdfDownloadListener.onDownloadSuccess()
            }

            override fun onError(error: Error?) {
                pdfDownloadListener.onDownloadFailed()
            }
        })
    }

    fun cancel() {
        if (::prDownloader.isInitialized) {
            prDownloader.cancel()
        }
    }

    fun isDownloaded(): Boolean {
       return file?.isFile == true
    }

    fun get(): ByteArray? {
        return file?.let { fileEncryptionAndDecryption.decrypt(it) }
    }
}

interface PdfDownloadListener {
    fun onDownloadSuccess()
    fun onDownloadFailed()
}
