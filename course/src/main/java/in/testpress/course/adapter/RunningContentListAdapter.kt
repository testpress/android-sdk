package `in`.testpress.course.adapter

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.databinding.RunningContentListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.util.ViewUtils
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RunningContentListAdapter :
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
        val binding = RunningContentListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RunningContentViewHolder(binding)
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

class RunningContentViewHolder(binding: RunningContentListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val title = binding.runningContentTitle
    private val path = binding.treePath
    private val date = binding.startDateAndEndDate
    private val image = binding.runningContentImage

    init {
        title.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
        path.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
        date.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
    }

    fun bind(content: DomainContent, clickListener: (DomainContent) -> Unit) {

        title.text = content.title
        path.text =content.treePath
            //"Courses > HYBRID ONL IIT - 2024 (INTEGRATED PROGRAMME) > MATHEMATICS > Trigonometry - I > Lecture Videos"
        date.text = content.getFormattedStartDateAndEndDate()
        image.setImageResource(setContentImage(content.contentType))
        itemView.setOnClickListener { clickListener(content) }
        setViewVisibility(content)
    }

    private fun setContentImage(contentType: String?): Int {
        return when (contentType) {
            "Attachment" -> R.drawable.paperclip
            "Video" -> R.drawable.play
            "Notes" -> R.drawable.writing
            "VideoConference" -> R.drawable.ic_live
            "Exam" -> R.drawable.test
            else -> R.drawable.test
        }
    }
    private fun setViewVisibility(content: DomainContent){
        if (content.getFormattedStartDateAndEndDate() != ""){
            ViewUtils.setGone(date,false)
        }
    }
}