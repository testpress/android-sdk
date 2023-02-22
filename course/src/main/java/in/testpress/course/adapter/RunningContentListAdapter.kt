package `in`.testpress.course.adapter

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.databinding.RunningUpcomingListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.createDomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.DateUtils
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.util.ViewUtils
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class RunningContentListAdapter:
    PagingDataAdapter<RunningContentEntity, BaseContentListViewHolder>(DOMAIN_CONTENT_COMPARATOR) {

    companion object {
        private val DOMAIN_CONTENT_COMPARATOR =
            object : DiffUtil.ItemCallback<RunningContentEntity>() {
                override fun areContentsTheSame(
                    oldItem: RunningContentEntity,
                    newItem: RunningContentEntity
                ): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: RunningContentEntity,
                    newItem: RunningContentEntity
                ): Boolean =
                    oldItem.id == newItem.id
            }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseContentListViewHolder {
        val binding = RunningUpcomingListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BaseContentListViewHolder(binding)
    }

    private fun onItemClick(content: DomainContent, context: Context) {
        context.startActivity(
            ContentActivity.createIntent(
                content.id,
                context,
                ""
            )
        )
    }

    override fun onBindViewHolder(holder: BaseContentListViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            holder.bind(createDomainContent(content)) { onItemClick(createDomainContent(content), holder.itemView.context) }
        }
    }
}

class BaseContentListViewHolder(binding: RunningUpcomingListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val title = binding.title
    private val path = binding.path
    private val date = binding.date
    private val thumbnail = binding.thumbnail

    init {
        title.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
        path.typeface = TestpressSdk.getRubikRegularFont(binding.root.context)
        date.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
    }

    fun bind(content: DomainContent, clickListener: (DomainContent) -> Unit) {
        title.text = content.title
        path.text = "${content.treePath}"
        thumbnail.setImageResource(getContentImage(content.contentType))
        itemView.setOnClickListener { clickListener(content) }
        date.text = "Ends ${DateUtils.getHumanizedDateFormat(content.end)} - "
        setDateViewVisibility(DateUtils.getHumanizedDateFormat(content.end))

    }

    private fun getContentImage(contentType: String?): Int {
        return when (contentType) {
            "Attachment" -> R.drawable.testpress_file_icon
            "Video" -> R.drawable.testpress_video_icon
            "Notes" -> R.drawable.testpress_notes_icon
            "VideoConference" -> R.drawable.testpress_live_conference_icon
            "Exam" -> R.drawable.testpress_exam_icon
            else -> R.drawable.testpress_exam_icon
        }
    }

    private fun setDateViewVisibility(dateText: String?) {
        ViewUtils.setGone(date, dateText.isNullOrEmpty())
    }
}