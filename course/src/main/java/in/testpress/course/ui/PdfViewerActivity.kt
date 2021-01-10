package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.util.DisplayPDF
import `in`.testpress.course.util.DisplayPDFListener
import `in`.testpress.course.util.PDFDownloadManager
import `in`.testpress.course.util.PdfDownloadListener
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_pdf_viewer.*
import kotlinx.android.synthetic.main.layout_pdf_viewer.downloadProgress
import kotlinx.android.synthetic.main.layout_pdf_viewer.emptyContainer
import kotlinx.android.synthetic.main.layout_pdf_viewer.pdfView
import kotlinx.android.synthetic.main.layout_pdf_viewer.progressPercentage

class PdfViewerActivity : AppCompatActivity(), PdfDownloadListener, DisplayPDFListener {

    private lateinit var pdfDownloadManager: PDFDownloadManager

    private var pageNumber = 0

    private lateinit var url: String

    private lateinit var fileName: String

    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pdf_viewer)
        hideStatusBar()
        getDataFromBundle()
        pdfDownloadManager = PDFDownloadManager(this,this,fileName)
        if (pdfDownloadManager.isDownloaded()) {
            displayPDF()
        } else {
            pdfDownloadManager.download(url)
        }
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
    }

    override fun onDownloadSuccess() {
        hideDownloadProgress()
        displayPDF()
    }

    private fun displayPDF() {
        progressbar.visibility = View.VISIBLE
        DisplayPDF(this,displayPDFListener = this).showPdfFromFile(
                pageNumber = pageNumber,
                file = pdfDownloadManager.get(),
                pdfView = pdfView
        )
    }

    override fun onDownloadFailed() {
        hideDownloadProgress()
        showErrorView()
    }

    override fun downloadProgress(progress: Int) {
        if (progressPercentage != null) {
            showDownloadProgress()
            downloadProgress.progress = progress
            progressPercentage.text = "$progress%"
        }
    }

    override fun onSingleTapOnPDF() {}

    override fun onPDFLoaded() {
        progressbar.visibility = View.GONE
    }

    override fun onError() {
        showErrorView()
    }

    override fun onPageChanged(pageNumber: Int) {
        this.pageNumber = pageNumber
    }

    private fun showDownloadProgress() {
        downloadProgress.visibility = View.VISIBLE
        progressPercentage.visibility = View.VISIBLE
    }

    private fun hideDownloadProgress() {
        downloadProgress.visibility = View.GONE
        progressPercentage.visibility = View.GONE
    }

    private fun showErrorView() {
        progressbar.visibility = View.GONE
        pdfView.visibility = View.GONE
        emptyContainer.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfDownloadManager.cancel()
        pdfDownloadManager.cleanup()
    }
}
