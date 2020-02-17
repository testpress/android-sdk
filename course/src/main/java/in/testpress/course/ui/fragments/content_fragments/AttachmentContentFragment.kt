package `in`.testpress.course.ui.fragments.content_fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.models.greendao.AttachmentDao
import `in`.testpress.models.greendao.Content
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

    private lateinit var attachmentDao: AttachmentDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachmentDao = TestpressSDKDatabase.getAttachmentDao(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.attachment_content_detail, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachmentContentLayout = view.findViewById(R.id.attachment_content_layout)
        titleView = view.findViewById(R.id.title)
        titleLayout = view.findViewById(R.id.title_layout)
        description = view.findViewById(R.id.description)
        downloadButton = view.findViewById(R.id.download_attachment)
        ViewUtils.setTypeface(arrayOf(titleView), TestpressSdk.getRubikMediumFont(activity!!))
        ViewUtils.setLeftDrawable(context, downloadButton, R.drawable.ic_file_download_18dp)
    }

    override fun loadContent() {
        titleView.text = content.title
        titleLayout.visibility = View.VISIBLE

        if (content.rawAttachment == null) {
            updateContent()
            return
        }

        val attachment = content.rawAttachment
        if (!attachment.description.isNullOrEmpty()) {
            description.text = attachment.description
            description.typeface = TestpressSdk.getRubikRegularFont(context!!)
        }

        downloadButton.setOnClickListener() {
            context!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(attachment.attachmentUrl)))
        }
        attachmentContentLayout.visibility = View.VISIBLE
        viewModel.createContentAttempt()
    }

    override fun onUpdateContent(content: Content) {
        val attachment = content.rawAttachment
        contentDao.deleteAll()
        attachment?.let {
            attachmentDao.insertOrReplaceInTx(it)
            content.attachmentId = it.id
            contentDao.delete(content)
            contentDao.insertOrReplaceInTx(content)
        }
    }
}
