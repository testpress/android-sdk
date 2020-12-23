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
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.layout_document_viewer.*
import kotlinx.android.synthetic.main.layout_document_viewer.pdfView
import java.net.URI

class DocumentViewerFragment : BaseContentDetailFragment(), PdfDownloadListener,
        DisplayPDFListener {

    private var pageNumber = 0

    private lateinit var fileName: String

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var pdfDownloadManager: PDFDownloadManager

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    lateinit var fullScreenMenu: MenuItem
    private val completeProgress = 100

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
            viewModel.createContentAttempt(contentId).observe(viewLifecycleOwner, Observer {
                checkAndUnlockNextContent()
            })
        } else {
            content.attachment?.attachmentUrl?.let {
                pdfDownloadManager.download(it)
            }
        }
    }

    private fun getFileName(): String {
        var filename = content.attachment?.title ?: ""
        val attachmentURI = URI(content.attachment?.attachmentUrl)
        filename += attachmentURI.path
        return filename.generateSha256()
    }

    override fun onDownloadSuccess() {
        hideDownloadProgress()
        if (!DocumentViewerFragment().isDetached) {
            displayPDF()
        }
        viewModel.createContentAttempt(contentId).observe(viewLifecycleOwner, Observer {
            checkAndUnlockNextContent()
        })
    }

    private fun displayPDF() {
        progressBar.visibility = View.VISIBLE
        DisplayPDF(requireContext(),displayPDFListener = this).showPdfFromFile(
                file = pdfDownloadManager.get(),
                pdfView = pdfView
        )
    }

    override fun onDownloadFailed() {
        hideDownloadProgress()
        showErrorView()
    }

    override fun downloadProgress(progress: Int) {
        if (downloadProgress != null) {
            showDownloadProgress()
            downloadProgress.progress = progress
            progressPercentage.text = "$progress%"
            progressBar.visibility = View.GONE
        }
        if (progress == completeProgress) {
            hideDownloadProgress()
            progressBar.visibility = View.VISIBLE
        }
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

    private fun showDownloadProgress() {
        downloadProgress.visibility = View.VISIBLE
        progressPercentage.visibility = View.VISIBLE
    }

    private fun hideDownloadProgress() {
        if (downloadProgress != null) {
            downloadProgress.visibility = View.GONE
            progressPercentage.visibility = View.GONE
        }
    }

    private fun showErrorView() {
        pdfView.visibility = View.GONE
        emptyContainer.visibility = View.VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        pdfDownloadManager.cancel()
        pdfDownloadManager.cleanup()
    }
}
