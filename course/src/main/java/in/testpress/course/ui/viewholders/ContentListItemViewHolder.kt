package `in`.testpress.course.ui.viewholders

import `in`.testpress.course.R
import `in`.testpress.course.domain.ContentType
import `in`.testpress.course.domain.DomainContent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ContentListItemViewHolder(view: View) : BaseContentListItemViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): ContentListItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_list_item, parent, false)
            return ContentListItemViewHolder(view)
        }

    }

    override fun bindContentDetails(content: DomainContent) {
        super.bindContentDetails(content)
        when (content.contentTypeEnum) {
            ContentType.Notes -> contentIcon.setImageResource(R.drawable.writing)
            ContentType.Attachment -> contentIcon.setImageResource(R.drawable.paperclip)
            else -> contentIcon.setImageResource(R.drawable.ic_live)
        }
    }
}
