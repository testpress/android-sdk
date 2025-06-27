package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.util.PermissionsUtils
import `in`.testpress.util.ViewUtils
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import `in`.testpress.course.viewmodels.OfflineAttachmentViewModel
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
import `in`.testpress.util.getFileExtensionFromUrl
import kotlinx.coroutines.launch
import java.io.File

class AttachmentContentFragment : BaseContentDetailFragment() {
    private lateinit var attachmentContentLayout: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var description: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var actionButton: Button
    private lateinit var permissionsUtils: PermissionsUtils
    private lateinit var offlineAttachmentViewModel: OfflineAttachmentViewModel

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
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE

        val attachment = content.attachment!!
        if (!attachment.description.isNullOrEmpty()) {
            description.text = attachment.description
        }

        viewLifecycleOwner.lifecycleScope.launch {
            offlineAttachmentViewModel.syncDownloadedFileWithDatabase(requireContext(), attachment.id)

            offlineAttachmentViewModel.getOfflineAttachment(attachment.id)
                .collect { offlineAttachment ->
                    when (offlineAttachment?.status) {
                        OfflineAttachmentDownloadStatus.QUEUED -> {
                            actionButton.text = "Waiting to download"
                            actionButton.isClickable = false
                        }

                        OfflineAttachmentDownloadStatus.DOWNLOADING -> {
                            actionButton.text = "Downloading...%${offlineAttachment.progress}"
                            actionButton.isClickable = false
                        }
                        OfflineAttachmentDownloadStatus.COMPLETED -> {
                            actionButton.text = "Open Attachment"
                            actionButton.isClickable = true
                            actionButton.setOnClickListener {
                                offlineAttachmentViewModel.openFile(
                                    requireContext(),
                                    offlineAttachment
                                )
                            }
                        }
                        OfflineAttachmentDownloadStatus.FAILED -> {
                            actionButton.text = "Download Attachment"
                            actionButton.isClickable = true
                            offlineAttachmentViewModel.delete(offlineAttachment.id)
                            Toast.makeText(requireContext(),"Download Failed", Toast.LENGTH_SHORT).show()
                            actionButton.setOnClickListener {
                                onDownloadClick(attachment)
                            }
                        }
                        OfflineAttachmentDownloadStatus.DELETE -> {
                            actionButton.text = "Download Attachment"
                            actionButton.isClickable = true
                            offlineAttachmentViewModel.delete(offlineAttachment.id)
                            Toast.makeText(requireContext(),"Download has been removed", Toast.LENGTH_SHORT).show()
                            actionButton.setOnClickListener {
                                onDownloadClick(attachment)
                            }
                        }

                        null -> {
                            actionButton.text = "Download Attachment"
                            actionButton.isClickable = true
                            actionButton.setOnClickListener {
                                onDownloadClick(attachment)
                            }
                        }
                    }
                }
        }

        attachmentContentLayout.visibility = View.VISIBLE
        viewModel.createContentAttempt(contentId)
    }

    private fun onDownloadClick(attachment: DomainAttachmentContent) {
        val destinationPath = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), attachment.title!!
        ).path + getFileExtensionFromUrl(attachment.attachmentUrl)

        val fileName = File(destinationPath).name

        offlineAttachmentViewModel.requestDownload(
            requireContext(),
            attachment,
            destinationPath = destinationPath,
            fileName = fileName
        )
    }

    override fun onResume() {
        super.onResume()
        if (isContentInitialized()) {
            content.attachmentId?.let {
                viewLifecycleOwner.lifecycleScope.launch {
                    offlineAttachmentViewModel.syncDownloadedFileWithDatabase(requireContext(), it)
                }
            }
        }
    }
}