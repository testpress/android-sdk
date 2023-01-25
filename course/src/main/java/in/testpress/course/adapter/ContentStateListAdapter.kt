package `in`.testpress.course.adapter

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.databinding.ContentStateListItemV2Binding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.fragments.UPCOMING_CONTENTS_FRAGMENT_TAG
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.DateUtils
import `in`.testpress.util.ViewUtils
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ContentStateListAdapter(private val fragmentTag: String) :
    ListAdapter<DomainContent, ContentStateViewHolder>(DOMAIN_CONTENT_COMPARATOR) {

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
    ): ContentStateViewHolder {
        val binding = ContentStateListItemV2Binding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContentStateViewHolder(binding)
    }

    private fun onItemClick(content: DomainContent, context: Context) {
        if (fragmentTag == UPCOMING_CONTENTS_FRAGMENT_TAG){
            Toast.makeText(
                context,
                "Available ${DateUtils.getHumanizedDateFormat(content.start)}",
                Toast.LENGTH_SHORT
            ).show()
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

    override fun onBindViewHolder(holder: ContentStateViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            holder.bind(content,fragmentTag) { onItemClick(content, holder.itemView.context) }
        }
    }
}

class ContentStateViewHolder(binding: ContentStateListItemV2Binding) :
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

    fun bind(content: DomainContent,fragmentTag: String, clickListener: (DomainContent) -> Unit) {
        title.text = content.title
        path.text ="${content.treePath}"
        thumbnail.setImageResource(getContentImage(content.contentType))
        itemView.setOnClickListener { clickListener(content) }
        if (fragmentTag == UPCOMING_CONTENTS_FRAGMENT_TAG){
            date.text = "Available ${DateUtils.getHumanizedDateFormat(content.start)} - "
            setDateViewVisibility(DateUtils.getHumanizedDateFormat(content.start))
        } else {
            date.text = "Ends ${DateUtils.getHumanizedDateFormat(content.end)} - "
            setDateViewVisibility(DateUtils.getHumanizedDateFormat(content.end))
        }
        Log.d("TAG", "bind: ${DateUtils.getHumanizedDateFormat(content.end)}")
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

    private fun setDateViewVisibility(dateText: String?){
        if (dateText == null || dateText == "") {
            ViewUtils.setGone(date,true)
        } else {
            ViewUtils.setGone(date,false)
        }
    }
}