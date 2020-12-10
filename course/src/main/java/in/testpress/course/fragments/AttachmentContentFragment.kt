package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.ui.PdfViewerActivity
import `in`.testpress.util.ViewUtils
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AttachmentContentFragment : BaseContentDetailFragment() {
    private lateinit var attachmentContentLayout: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var description: TextView
    private lateinit var titleLayout: LinearLayout
    private lateinit var downloadButton: Button
    private lateinit var viewAttachmentButton: Button

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
        viewAttachmentButton = view.findViewById(R.id.view_attachment)
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

        if (attachment.isRenderable == true) {
            viewAttachmentButton.visibility = View.VISIBLE
            downloadButton.visibility = View.GONE
        } else {
            downloadButton.visibility = View.VISIBLE
            viewAttachmentButton.visibility = View.GONE
        }

        setClickListeners(attachment.attachmentUrl)
        attachmentContentLayout.visibility = View.VISIBLE
        viewModel.createContentAttempt(contentId)
    }

    private fun setClickListeners(attachmentUrl: String?) {
        downloadButton.setOnClickListener {
            forceReloadContent {
                context!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(attachmentUrl)))
            }
        }

        viewAttachmentButton.setOnClickListener {
            forceReloadContent {
                navigateToPdfViewerActivity(attachmentUrl)
            }
        }
    }

    private fun navigateToPdfViewerActivity(attachmentUrl: String?) {
         startActivity(Intent(this.activity, PdfViewerActivity::class.java).apply {
            putExtra("pdfUrl", attachmentUrl)
        })
    }
}