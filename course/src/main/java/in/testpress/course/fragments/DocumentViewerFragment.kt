package `in`.testpress.course.fragments

import `in`.testpress.Constants.DEFAULT_ATTACHMENT_TITLE
import `in`.testpress.course.R
import `in`.testpress.course.databinding.LayoutDocumentViewerBinding
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.ui.PdfViewerActivity
import `in`.testpress.course.util.PDFViewer
import `in`.testpress.course.util.DisplayPDFListener
import `in`.testpress.course.util.PDFDownloadManager
import `in`.testpress.course.util.PdfDownloadListener
import `in`.testpress.course.util.SHA256Generator.generateSha256
import `in`.testpress.databinding.DiscussionListBinding
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Observer
import java.net.URI

class DocumentViewerFragment : BaseContentDetailFragment(), PdfDownloadListener,
        DisplayPDFListener {
    private var _binding: LayoutDocumentViewerBinding? = null
    private val binding get() = _binding!!
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
    ): View {
        _binding = LayoutDocumentViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        (activity as ContentActivity).setActionBarTitle(content.attachment?.title?:DEFAULT_ATTACHMENT_TITLE)
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
        binding.encryptionProgress.visibility = View.VISIBLE
        PDFViewer(requireContext(),displayPDFListener = this).display(
                file = pdfDownloadManager.get(),
                pdfView = binding.pdfView
        )
    }

    override fun onDownloadFailed() {
        hideDownloadProgress()
        showErrorView()
    }

    override fun downloadProgress(progress: Int) {
        showDownloadProgress(progress)
        binding.encryptionProgress.visibility = View.GONE
        if (progress == completeProgress) {
            hideDownloadProgress()
            binding.encryptionProgress.visibility = View.VISIBLE
        }
    }

    override fun onSingleTapOnPDF() {
        navigateToPdfViewerActivity()
    }

    override fun onPageChanged(pageNumber: Int) {
        this.pageNumber = pageNumber
    }

    override fun onPDFLoaded() {
        binding.encryptionProgress.visibility = View.GONE
        if (::fullScreenMenu.isInitialized) {
            fullScreenMenu.isVisible = true
        }
    }

    override fun onError() {
        showErrorView()
    }

    private fun showDownloadProgress(progress: Int) {
        binding.downloadProgress.visibility = View.VISIBLE
        binding.progressPercentage.visibility = View.VISIBLE
        binding.downloadProgress.progress = progress
        binding.progressPercentage.text = "$progress%"
    }

    private fun hideDownloadProgress() {
        binding.downloadProgress.visibility = View.GONE
        binding.progressPercentage.visibility = View.GONE
    }

    private fun showErrorView() {
        binding.pdfView.visibility = View.GONE
        binding.emptyContainer.visibility = View.VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
        if (::pdfDownloadManager.isInitialized) {
            pdfDownloadManager.cancel()
            pdfDownloadManager.cleanup()
        }
    }
}
