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

    private var fileEncryptionAndDecryption = FileEncryptionAndDecryption(context)

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
                        file?.let { fileEncryptionAndDecryption.encrypt(it) }
                        pdfDownloadListener.onDownloadSuccess()
                    }

                    override fun onError(error: com.downloader.Error?) {
                        pdfDownloadListener.onDownloadFailed()
                    }
                })
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
