package `in`.testpress.course.ui.viewholders

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.ContentType
import `in`.testpress.course.domain.DomainContent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class ContentListItemViewHolder(view: View) : BaseContentListItemViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): ContentListItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.content_list_item, parent, false)
            return ContentListItemViewHolder(view)
        }

    }
}