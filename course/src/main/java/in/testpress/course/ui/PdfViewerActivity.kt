package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.util.DisplayPDF
import `in`.testpress.course.util.DisplayPDFListener
import `in`.testpress.course.util.PDFDownloader
import `in`.testpress.course.util.PdfDownloadListener
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.layout_pdf_viewer.*

class PdfViewerActivity : AppCompatActivity(), PdfDownloadListener, DisplayPDFListener {

    private lateinit var pdfDownloader: PDFDownloader

    private var pageNumber = 0

    private lateinit var url: String

    private lateinit var fileName: String

    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pdf_viewer)
        hideStatusBar()
        getDataFromBundle()
        pdfDownloader = PDFDownloader(this,this,fileName)
        if (pdfDownloader.isDownloaded()) {
            displayPDF()
        } else {
            pdfDownloader.download(url)
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
        displayPDF()
    }

    private fun displayPDF() {
        DisplayPDF(this,displayPDFListener = this).showPdfFromFile(
                pageNumber = pageNumber,
                password = password,
                file = pdfDownloader.get(),
                pdfView = pdfView
        )
    }

    override fun onDownloadFailed() {
        showErrorView()
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

    private fun showErrorView() {
        progressbar.visibility = View.GONE
        pdfView.visibility = View.GONE
        emptyContainer.visibility = View.VISIBLE
    }
}
