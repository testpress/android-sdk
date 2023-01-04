package `in`.testpress.course.adapter

import `in`.testpress.course.domain.ContentType
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.ui.viewholders.BaseContentListItemViewHolder
import `in`.testpress.course.ui.viewholders.ContentListItemViewHolder
import `in`.testpress.course.ui.viewholders.ExamContentListItemViewHolder
import `in`.testpress.course.ui.viewholders.VideoContentListItemViewHolder
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class RunningContentListAdapter :
    ListAdapter<DomainContent, BaseContentListItemViewHolder>(DOMAIN_CONTENT_COMPARATOR) {

    var contents: List<DomainContent> = listOf()

    companion object {
        private val DOMAIN_CONTENT_COMPARATOR = object : DiffUtil.ItemCallback<DomainContent>() {
            override fun areContentsTheSame(
                oldItem: DomainContent,
                newItem: DomainContent
            ): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: DomainContent, newItem: DomainContent): Boolean =
                oldItem.id == newItem.id
        }
    }

    override fun getItemCount(): Int {
        return contents.size
    }

    override fun getItem(position: Int): DomainContent? {
        if (contents.size > position) return contents[position]
        return null
    }

    override fun getItemViewType(position: Int): Int {
        val content = getItem(position)
        return content?.contentTypeEnum?.ordinal ?: 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseContentListItemViewHolder {
        return when (viewType) {
            ContentType.Exam.ordinal -> ExamContentListItemViewHolder.create(parent)
            ContentType.Quiz.ordinal -> ExamContentListItemViewHolder.create(parent)
            ContentType.Video.ordinal -> VideoContentListItemViewHolder.create(parent)
            ContentType.Notes.ordinal -> ContentListItemViewHolder.create(parent)
            ContentType.Attachment.ordinal -> ContentListItemViewHolder.create(parent)
            else -> ContentListItemViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: BaseContentListItemViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            holder.bind(content, false) {
                onItemClick(it, holder.itemView.context)
            }
        }
    }

    private fun onItemClick(content: DomainContent, context: Context) {
        context.startActivity(
            ContentActivity.createIntent(
                content.id,
                context,
                ""
            )
        );

    }
}