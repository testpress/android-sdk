package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.util.PermissionsUtils
import `in`.testpress.util.ViewUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import `in`.testpress.course.util.FileUtils.getFileExtensionFromUrl
import `in`.testpress.course.viewmodels.OfflineAttachmentViewModel
import `in`.testpress.database.entities.OfflineAttachmentDownloadStatus
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
            offlineAttachmentViewModel.getOfflineAttachment(attachment.id)
                .collect { offlineAttachment ->
                    when (offlineAttachment?.status) {
                        OfflineAttachmentDownloadStatus.FAILED -> {
                            actionButton.setOnClickListener {
                                onDownloadClick(attachment)
                            }
                        }

                        OfflineAttachmentDownloadStatus.DOWNLOADED -> {
                            actionButton.text = "Open Attachment"
                            actionButton.setOnClickListener {
                                offlineAttachmentViewModel.openFile(
                                    requireContext(),
                                    offlineAttachment
                                )
                            }
                        }

                        OfflineAttachmentDownloadStatus.QUEUED -> {
                            actionButton.text = "Waiting to download"
                            actionButton.isClickable = false
                        }

                        OfflineAttachmentDownloadStatus.DOWNLOADING -> {
                            actionButton.text = "Downloading...%${offlineAttachment.progress}"
                            actionButton.isClickable = false
                        }

                        null -> {
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
        offlineAttachmentViewModel.requestDownload(
            attachment = attachment,
            destinationPath = File(
                "${requireActivity().filesDir}/offline_attachments/",
                attachment.title!!
            ).path + getFileExtensionFromUrl(attachment.attachmentUrl)
        )
    }
}