package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.ui.PdfViewerActivity
import `in`.testpress.course.util.PDFDownloader
import `in`.testpress.course.util.PdfDownloadListener
import `in`.testpress.course.util.SHA256Generator.getSha256
import android.content.Intent
import android.os.Bundle
import android.view.*
import com.downloader.PRDownloader
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.android.synthetic.main.layout_document_viewer.*
import kotlinx.android.synthetic.main.layout_document_viewer.emptyContainer
import kotlinx.android.synthetic.main.layout_pdf_viewer.pdfView
import kotlinx.android.synthetic.main.layout_pdf_viewer.progressbar


class DocumentViewerFragment : BaseContentDetailFragment(), PdfDownloadListener,
        OnPageChangeListener, OnErrorListener, OnLoadCompleteListener, OnPageErrorListener {

    private var pageNumber = 0

    private lateinit var fileName: String

    private lateinit var pdfDownloader: PDFDownloader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_document_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PRDownloader.initialize(requireContext())
        pdfDownloader = PDFDownloader(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.full_screen_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.fullScreen -> {
                navigateToPdfViewerActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToPdfViewerActivity() {
        startActivity(Intent(this.activity, PdfViewerActivity::class.java).apply {
            putExtra("pdfUrl", content.attachment?.attachmentUrl)
            putExtra("pageNumber", pageNumber)
            putExtra("password", contentId.toString())
            putExtra("fileName", fileName)
        })
    }

    override fun display() {
        (activity as ContentActivity).setActionBarTitle(content.attachment?.title)
        fileName = getFileName().getSha256()
        content.attachment?.attachmentUrl?.let {
            pdfDownloader.download(it, requireContext(), fileName)
        }
    }

    private fun getFileName(): String {
        var filename = contentId.toString()
        filename += content.attachment?.title ?: ""
        filename += content.attachment?.attachmentUrl
        return filename
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
                .password(contentId.toString())
                .swipeHorizontal(true)
                .onError {
                    showErrorView()
                }
                .onLoad {
                    progressbar.visibility = View.GONE
                }
                .onTap {
                    navigateToPdfViewerActivity()
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
    }

    override fun loadComplete(nbPages: Int) {
        progressBar.visibility = View.GONE
    }

    override fun onPageError(page: Int, t: Throwable?) {
        showErrorView()
    }

    override fun onError(t: Throwable?) {
        showErrorView()
    }

    private fun showErrorView() {
        pdfView.visibility = View.GONE
        emptyContainer.visibility = View.VISIBLE
    }
}
