package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainAttachmentContent
import `in`.testpress.util.FileDownloader
import `in`.testpress.util.ViewUtils
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.io.File

class AttachmentContentFragment : BaseContentDetailFragment() {
    private lateinit var attachmentContentLayout: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var description: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var downloadButton: Button

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
        description.typeface = TestpressSdk.getRubikRegularFont(context!!)
        downloadButton = view.findViewById(R.id.download_attachment)
        ViewUtils.setTypeface(arrayOf(titleView), TestpressSdk.getRubikMediumFont(activity!!))
        ViewUtils.setLeftDrawable(context, downloadButton, R.drawable.ic_file_download_18dp)
    }

    override fun display() {
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE

        val attachment = content.attachment!!
        if (!attachment.description.isNullOrEmpty()) {
            description.text = attachment.description
        }

        downloadButton.setOnClickListener {
            forceReloadContent {
                downloadFile(attachment)
            }
        }
        attachmentContentLayout.visibility = View.VISIBLE
        viewModel.createContentAttempt(contentId)
    }

    private fun downloadFile(attachment: DomainAttachmentContent){
        if (isDownloadUrlAvailable(attachment.attachmentUrl)){
            val fileDownloader = FileDownloader(requireContext())
            fileDownloader.downloadFile(
                attachment.attachmentUrl!!,
                "${attachment.title!!}${getFileType(attachment.attachmentUrl)}"
            )
            Toast.makeText(requireContext(),"Download Started...",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(),"File not available, Please try-again later",Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDownloadUrlAvailable(url:String?) = !url.isNullOrEmpty()

    private fun getFileType(url: String): String {
        val uri = Uri.parse(url)
        val filename = uri.lastPathSegment ?: ""
        val extension = File(filename).extension
        return if (extension.isNotBlank()) ".${extension}" else ""
    }

}