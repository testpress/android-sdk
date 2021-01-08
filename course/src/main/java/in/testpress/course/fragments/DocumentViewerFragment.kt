package `in`.testpress.course.fragments

import `in`.testpress.course.R
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.ui.PdfViewerActivity
import `in`.testpress.course.util.DisplayPDF
import `in`.testpress.course.util.DisplayPDFListener
import `in`.testpress.course.util.PDFDownloadManager
import `in`.testpress.course.util.PdfDownloadListener
import `in`.testpress.course.util.SHA256Generator.generateSha256
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.VisibleForTesting
import kotlinx.android.synthetic.main.layout_document_viewer.*
import kotlinx.android.synthetic.main.layout_document_viewer.pdfView

class DocumentViewerFragment : BaseContentDetailFragment(), PdfDownloadListener,
        DisplayPDFListener {

    private var pageNumber = 0

    private lateinit var fileName: String

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var pdfDownloader: PDFDownloadManager

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var fullScreenMenu: MenuItem

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.full_screen_menu, menu)
        fullScreenMenu = menu.findItem(R.id.fullScreen)
        fullScreenMenu.isVisible = false
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
        fileName = getFileName()
        pdfDownloadManager = PDFDownloadManager(this,requireContext(),fileName)
        if (pdfDownloadManager.isDownloaded()) {
            displayPDF()
            viewModel.createContentAttempt(contentId)
        } else {
            content.attachment?.attachmentUrl?.let {
                pdfDownloadManager.download(it)
            }
        }
    }

    private fun getFileName(): String {
        var filename = content.attachment?.title ?: ""
        filename += content.attachment?.attachmentUrl
        return filename.generateSha256()
    }

    override fun onDownloadSuccess() {
        if (!DocumentViewerFragment().isDetached) {
            displayPDF()
        }
        viewModel.createContentAttempt(contentId)
    }

    private fun displayPDF() {
        progressBar.visibility = View.VISIBLE
        DisplayPDF(requireContext(),displayPDFListener = this).showPdfFromFile(
                file = pdfDownloadManager.get(),
                pdfView = pdfView
        )
    }

    override fun onDownloadFailed() {
        showErrorView()
    }

    override fun onSingleTapOnPDF() {
        navigateToPdfViewerActivity()
    }

    override fun onPageChanged(pageNumber: Int) {
        this.pageNumber = pageNumber
    }

    override fun onPDFLoaded() {
        progressBar.visibility = View.GONE
        if (::fullScreenMenu.isInitialized) {
            fullScreenMenu.isVisible = true
        }
    }

    override fun onError() {
        showErrorView()
    }

    private fun showErrorView() {
        pdfView.visibility = View.GONE
        emptyContainer.visibility = View.VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        pdfDownloadManager.cancel()
    }
}
