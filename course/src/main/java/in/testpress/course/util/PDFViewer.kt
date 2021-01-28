package `in`.testpress.course.util

import `in`.testpress.util.CommonUtils.getUserName
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnDrawListener
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File

class PDFViewer(
        val context: Context,
        private val displayPDFListener: DisplayPDFListener
) : OnPageChangeListener, OnErrorListener, OnLoadCompleteListener, OnPageErrorListener,
    OnDrawListener {

    private val randomX = (0..10).random()
    private val randomY = (0..10).random()

    fun display(pageNumber: Int = 0, file: File, pdfView: PDFView) {
        pdfView.fromFile(file)
                .enableSwipe(true)
                .enableDoubletap(true)
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
                .onDraw(this)
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

    override fun onLayerDrawn(
        canvas: Canvas?,
        pageWidth: Float,
        pageHeight: Float,
        displayedPage: Int
    ) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.textSize = 80F
        paint.alpha = 40
        val randomHorizontalPosition = pageWidth/((randomX * (displayedPage + 1)) % 12.5F)
        val randomVerticalPosition = pageHeight/(((randomY * (displayedPage + 1)) % 12.5F))
        canvas?.drawText(getUserName(context),  randomHorizontalPosition, randomVerticalPosition, paint)
    }
}

interface DisplayPDFListener {
    fun onSingleTapOnPDF()
    fun onPDFLoaded()
    fun onError()
    fun onPageChanged(pageNumber: Int)
}
