package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.util.PermissionsUtils
import `in`.testpress.util.ViewUtils
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import `in`.testpress.course.viewmodels.OfflineAttachmentViewModel
import `in`.testpress.database.entities.OfflineAttachment
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class AttachmentContentFragment : BaseContentDetailFragment() {
    private lateinit var attachmentContentLayout: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var description: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var actionButton: Button
    private lateinit var permissionsUtils: PermissionsUtils
    private lateinit var offlineAttachmentViewModel: OfflineAttachmentViewModel
    private var downloadStatusJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        offlineAttachmentViewModel = OfflineAttachmentViewModel.get(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.attachment_content_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachmentContentLayout = view.findViewById(R.id.attachment_content_layout)
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
        description = view.findViewById(R.id.description)
        description.typeface = TestpressSdk.getRubikRegularFont(requireContext())
        actionButton = view.findViewById(R.id.download_attachment)
        ViewUtils.setTypeface(arrayOf(titleView), TestpressSdk.getRubikMediumFont(requireActivity()))
        ViewUtils.setLeftDrawable(context, actionButton, R.drawable.ic_file_download_18dp)
        permissionsUtils = PermissionsUtils(requireActivity(), view)
    }

    override fun display() {
        showTitle()
        showDescription()
        observeDownloadStatus()
        attachmentContentLayout.visibility = View.VISIBLE
        viewModel.createContentAttempt(contentId)
    }

    private fun showTitle() {
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE
    }

    private fun showDescription() {
        content.attachment?.description
            ?.takeIf { it.isNotEmpty() }
            ?.let { description.text = it }
    }

    private fun observeDownloadStatus() {
        val attachment = content.attachment ?: return
        if (downloadStatusJob?.isActive == true) return

        downloadStatusJob = viewLifecycleOwner.lifecycleScope.launch {
            offlineAttachmentViewModel.getOfflineAttachment(attachment.id)
                .distinctUntilChanged()
                .collect { offlineAttachment ->
                when (offlineAttachment?.status) {
                    OfflineAttachmentDownloadStatus.FAILED -> {
                        handleDownloadFailed(offlineAttachment)
                    }

                    OfflineAttachmentDownloadStatus.DELETE -> {
                        handleDownloadDeleted(offlineAttachment)
                    }

                    OfflineAttachmentDownloadStatus.COMPLETED -> {
                        handleDownloadCompleted(offlineAttachment)
                    }

                    OfflineAttachmentDownloadStatus.QUEUED -> {
                        updateActionButton("Waiting to download", isClickable = false)
                    }

                    OfflineAttachmentDownloadStatus.DOWNLOADING -> {
                        val progressText = "Downloading...%${offlineAttachment.progress}"
                        updateActionButton(progressText, isClickable = false)
                    }
                    null-> {
                        updateActionButton("Download Attachment", true) {
                            onDownloadClick(content.attachment!!)
                        }
                    }
                }
            }
        }
    }

    private fun handleDownloadFailed(attachment: OfflineAttachment) {
        offlineAttachmentViewModel.deleteById(attachment.id)
        updateActionButton("Download Attachment", isClickable = true) {
            onDownloadClick(content.attachment!!)
        }
        showToast("Download Failed. Try Again")
    }

    private fun handleDownloadDeleted(attachment: OfflineAttachment) {
        offlineAttachmentViewModel.deleteById(attachment.id)
        updateActionButton("Download Attachment", isClickable = true) {
            onDownloadClick(content.attachment!!)
        }
        showToast("Download has been removed")
    }

    private fun handleDownloadCompleted(attachment: OfflineAttachment) {
        updateActionButton("Open Attachment", isClickable = true) {
            offlineAttachmentViewModel.openFile(requireContext(), attachment)
        }
    }

    private fun updateActionButton(text: String, isClickable: Boolean, onClick: (() -> Unit)? = null) {
        actionButton.text = text
        actionButton.isClickable = isClickable
        actionButton.setOnClickListener(null)
        onClick?.let { actionButton.setOnClickListener { it() } }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun onDownloadClick(attachment: DomainAttachmentContent) {
        if (permissionsUtils.isStoragePermissionGranted) {
            forceReloadContent {
                offlineAttachmentViewModel.requestDownload(
                    requireContext(),
                    attachment = attachment
                )
            }
        } else {
            permissionsUtils.requestStoragePermissionWithSnackbar()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        downloadStatusJob?.cancel()
        downloadStatusJob = null
    }
}