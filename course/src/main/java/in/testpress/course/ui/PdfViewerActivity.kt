package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.util.PDFDownloader
import `in`.testpress.course.util.PdfDownloadListener
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.android.synthetic.main.layout_pdf_viewer.*

class PdfViewerActivity : AppCompatActivity(), PdfDownloadListener, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    private lateinit var pdfDownloader: PDFDownloader

    private var pageNumber = 0

    private lateinit var url: String

    private lateinit var fileName: String

    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pdf_viewer)
        hideStatusBar()
        pdfDownloader = PDFDownloader(this)
        getDataFromBundle()
        pdfDownloader.download(url, this, fileName)
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

    override fun pdfDownloaded(response: Boolean) {
        if (response) {
            showPdfFromFile()
        } else {
            showErrorView()
        }
    }

    private fun showPdfFromFile() {
        val file = pdfDownloader.get()
        pdfView.fromFile(file)
                .enableSwipe(true)
                .enableDoubletap(true)
                .password(password)
                .swipeHorizontal(true)
                .onError {
                    showErrorView()
                }
                .onLoad {
                    progressbar.visibility = View.GONE
                }
                .spacing(0)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(DefaultScrollHandle(this))
                .onPageError(this)
                .enableAntialiasing(true)
                .defaultPage(pageNumber)
                .load()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNumber = page
    }

    override fun loadComplete(nbPages: Int) {
        progressbar.visibility = View.GONE
    }

    override fun onPageError(page: Int, t: Throwable?) {
        showErrorView()
    }

    private fun showErrorView() {
        progressbar.visibility = View.GONE
        pdfView.visibility = View.GONE
        emptyContainer.visibility = View.VISIBLE
    }
}
