package `in`.testpress.course.util

import android.content.Context
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File

class DisplayPDF(
        val context: Context,
        private val displayPDFListener: DisplayPDFListener
) : OnPageChangeListener, OnErrorListener, OnLoadCompleteListener, OnPageErrorListener {

    fun showPdfFromFile(pageNumber: Int = 0, password: String? = null, file: File?, pdfView: PDFView) {
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
        displayPDFListener.onPageChanged(page)
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
