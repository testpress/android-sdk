package `in`.testpress.course.adapter

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.databinding.ContentStateListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.DateUtils
import `in`.testpress.util.ViewUtils
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ContentStateListAdapter(private val fragmentTag: String) :
    ListAdapter<DomainContent, RunningContentViewHolder>(DOMAIN_CONTENT_COMPARATOR) {

    var contents: List<DomainContent> = listOf()

    companion object {
        private val DOMAIN_CONTENT_COMPARATOR =
            object : DiffUtil.ItemCallback<DomainContent>() {
                override fun areContentsTheSame(
                    oldItem: DomainContent,
                    newItem: DomainContent
                ): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: DomainContent,
                    newItem: DomainContent
                ): Boolean =
                    oldItem.id == newItem.id
            }
    }

    override fun getItemCount(): Int {
        return contents.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RunningContentViewHolder {
        val binding = ContentStateListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RunningContentViewHolder(binding)
    }

    private fun onItemClick(content: DomainContent, context: Context) {
        if (fragmentTag == "Upcoming" && DateUtils.getFormattedStartDate(content.start) != ""){
            Toast.makeText(context,"This will be available ${DateUtils.getFormattedStartDate(content.start)}",Toast.LENGTH_SHORT).show()
        } else if (fragmentTag == "Upcoming") {
            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        } else{
            context.startActivity(
                ContentActivity.createIntent(
                    content.id,
                    context,
                    ""
                )
            )
        }
    }

    override fun getItem(position: Int): DomainContent? {
        if (contents.size > position) return contents[position]
        return null
    }

    override fun onBindViewHolder(holder: RunningContentViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            holder.bind(content) { onItemClick(content, holder.itemView.context) }
        }
    }
}

class RunningContentViewHolder(binding: ContentStateListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val title = binding.runningContentTitle
    private val path = binding.treePath
    private val date = binding.startDateAndEndDate
    private val image = binding.runningContentImage
    private val thumbnail = binding.thumbnail

    init {
        title.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
        path.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
        date.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
    }

    fun bind(content: DomainContent, clickListener: (DomainContent) -> Unit) {
        title.text = content.title
        path.text =content.treePath
        date.text = DateUtils.getFormattedStartDateAndEndDate(content.start,content.end)
        image.setImageResource(getContentImage(content.contentType))
        thumbnail.setImageResource(getBackgroundColour(content.contentType))
        itemView.setOnClickListener { clickListener(content) }
        setViewVisibility(content)
    }

    private fun getContentImage(contentType: String?): Int {
        return when (contentType) {
            "Attachment" -> R.drawable.paperclip
            "Video" -> R.drawable.play
            "Notes" -> R.drawable.writing
            "VideoConference" -> R.drawable.ic_live
            "Exam" -> R.drawable.test
            else -> R.drawable.test
        }
    }

    private fun getBackgroundColour(contentType: String?): Int {
        return when (contentType) {
            "Attachment" -> R.color.testpress_attachments_and_pdf_color
            "Video" -> R.color.testpress_video_color
            "Notes" -> R.color.testpress_notes_color
            "VideoConference" -> R.color.testpress_live_stream_color
            "Exam" -> R.color.testpress_exam_color
            else -> R.color.testpress_default_color
        }
    }
    private fun setViewVisibility(content: DomainContent){
        if (DateUtils.getFormattedStartDateAndEndDate(content.start,content.end) != ""){
            ViewUtils.setGone(date,false)
        }
    }
}