package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.databinding.LayoutPdfViewerBinding
import `in`.testpress.course.util.PDFViewer
import `in`.testpress.course.util.DisplayPDFListener
import `in`.testpress.course.util.PDFDownloadManager
import `in`.testpress.course.util.PdfDownloadListener
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PdfViewerActivity : AppCompatActivity(), PdfDownloadListener, DisplayPDFListener {
    private lateinit var binding: LayoutPdfViewerBinding
    private lateinit var pdfDownloadManager: PDFDownloadManager

    private var pageNumber = 0

    private lateinit var url: String

    private lateinit var fileName: String

    private lateinit var password: String

    private var pdfPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disableScreenShot()
        binding = LayoutPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideStatusBar()
        getDataFromBundle()

        if (!pdfPath.isNullOrBlank()) {
            displayLocalPDF(File(pdfPath!!))
        } else {
            pdfDownloadManager = PDFDownloadManager(this, this, fileName)
            if (pdfDownloadManager.isDownloaded()) {
                displayPDF()
            } else {
                pdfDownloadManager.download(url)
            }
        }
    }

    private fun disableScreenShot() {
        window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        this.window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun getDataFromBundle() {
        pageNumber = intent.getIntExtra("pageNumber", 0)
        password = intent.getStringExtra("password") ?: ""
        url = intent.getStringExtra("pdfUrl") ?: ""
        fileName = intent.getStringExtra("fileName") ?: ""
        pdfPath = intent.getStringExtra(PDF_PATH)
    }

    private fun displayLocalPDF(file: File) {
        val base = File(filesDir, "offline_attachments").canonicalFile
        val canonicalTarget = try {
            file.canonicalFile
        } catch (e: Exception) {
            showErrorView()
            return
        }

        if (!canonicalTarget.path.startsWith(base.path + File.separator) ||
            !canonicalTarget.exists() || !canonicalTarget.isFile) {
            showErrorView()
            return
        }

        binding.encryptionProgressbar.visibility = View.VISIBLE
        PDFViewer(this, displayPDFListener = this).display(
                pageNumber = pageNumber,
                file = canonicalTarget,
                pdfView = binding.pdfView
        )
    }

    override fun onDownloadSuccess() {
        hideDownloadProgress()
        displayPDF()
    }

    private fun displayPDF() {
        binding.encryptionProgressbar.visibility = View.VISIBLE
        PDFViewer(this,displayPDFListener = this).display(
                pageNumber = pageNumber,
                file = pdfDownloadManager.get(),
                pdfView = binding.pdfView
        )
    }

    override fun onDownloadFailed() {
        hideDownloadProgress()
        showErrorView()
    }

    override fun downloadProgress(progress: Int) {
        showDownloadProgress(progress)
    }

    override fun onSingleTapOnPDF() {}

    override fun onPDFLoaded() {
        binding.encryptionProgressbar.visibility = View.GONE
    }

    override fun onError() {
        showErrorView()
    }

    override fun onPageChanged(pageNumber: Int) {
        this.pageNumber = pageNumber
    }

    private fun showDownloadProgress(progress: Int) {
        binding.downloadProgress.visibility = View.VISIBLE
        binding.progressPercentage.visibility = View.VISIBLE
        binding.downloadProgress.progress = progress
        binding.progressPercentage.text = "$progress%"
    }

    private fun hideDownloadProgress() {
        binding.downloadProgress.visibility = View.GONE
        binding.progressPercentage.visibility = View.GONE
    }

    private fun showErrorView() {
        binding.encryptionProgressbar.visibility = View.GONE
        binding.pdfView.visibility = View.GONE
        binding.emptyContainer.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::pdfDownloadManager.isInitialized) {
            pdfDownloadManager.cancel()
            pdfDownloadManager.cleanup()
        }
    }

    companion object {
        const val PDF_PATH = "pdfPath"

        @JvmStatic
        fun createIntent(context: Context, pdfPath: String, pageNumber: Int = 0): Intent {
            return Intent(context, PdfViewerActivity::class.java).apply {
                putExtra(PDF_PATH, pdfPath)
                putExtra("pageNumber", pageNumber)
            }
        }
    }
}
