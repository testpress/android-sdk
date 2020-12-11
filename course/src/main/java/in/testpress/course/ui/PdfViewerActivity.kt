package `in`.testpress.course.ui

import `in`.testpress.course.R
import `in`.testpress.course.fragments.InputStreamListener
import `in`.testpress.course.fragments.PdfUtil
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.android.synthetic.main.layout_pdf_viewer.*
import java.io.InputStream

class PdfViewerActivity : AppCompatActivity(), InputStreamListener, OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {

    var inputStreamListener: InputStreamListener? = null

    private var pageNumber = 0

    private lateinit var url: String

    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pdf_viewer)
        hideStatusBar()
        inputStreamListener = this
        getDataFromBundle()
        PdfUtil(this).get(url)
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
    }

    override fun getResponse(response: InputStream?) {
        response?.let { loadPDF(it) }?: showErrorView()
    }

    private fun loadPDF(inputStream: InputStream) {
        pdfView.fromStream(inputStream)
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
