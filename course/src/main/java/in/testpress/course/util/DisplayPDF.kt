package `in`.testpress.course.util

import android.content.Context
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle

class DisplayPDF(
        val context: Context,
        private val displayPDFListener: DisplayPDFListener
) : OnPageChangeListener, OnErrorListener, OnLoadCompleteListener, OnPageErrorListener {

    private var pageNumber: Int = 0

    fun showPdfFromFile(pageNumber: Int, password: String, pdfDownloader: PDFDownloader, pdfView: PDFView) {
        val file = pdfDownloader.get()
        this.pageNumber = pageNumber
        pdfView.fromFile(file)
                .enableSwipe(true)
                .enableDoubletap(true)
                .password(password)
                .swipeHorizontal(true)
                .onError {
                    displayPDFListener.onError()
                }
                .onLoad {
                    displayPDFListener.onPDFLoaded()
                }
                .onTap {
                    displayPDFListener.onSingleTapOnPDF()
                    return@onTap true
                }
                .spacing(0)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(DefaultScrollHandle(this.context))
                .onPageError(this)
                .enableAntialiasing(true)
                .defaultPage(pageNumber)
                .load()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNumber = page
        displayPDFListener.onPageChanged(pageNumber)
    }

    override fun loadComplete(nbPages: Int) {
        displayPDFListener.onPDFLoaded()
    }

    override fun onPageError(page: Int, t: Throwable?) {
        displayPDFListener.onError()
    }

    override fun onError(t: Throwable?) {
        displayPDFListener.onError()
    }
}

interface DisplayPDFListener {
    fun onSingleTapOnPDF()
    fun onPDFLoaded()
    fun onError()
    fun onPageChanged(pageNumber: Int)
}
