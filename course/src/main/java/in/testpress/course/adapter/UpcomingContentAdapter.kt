package `in`.testpress.course.adapter

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.databinding.UpcomignContentHeaderListItemBinding
import `in`.testpress.course.databinding.UpcomingContentListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.util.ViewUtils
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter

class UpcomingContentAdapter :
    ListAdapter<Any, UpcomingContentViewHolder>(DOMAIN_CONTENT_COMPARATOR) {

    var contents: List<Any> = listOf()

    companion object {
        private val DOMAIN_CONTENT_COMPARATOR =
            object : DiffUtil.ItemCallback<Any>() {
                override fun areContentsTheSame(
                    oldItem: Any,
                    newItem: Any
                ): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: Any,
                    newItem: Any
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
    ): UpcomingContentViewHolder {
        val binding = when(viewType){
            UpcomingContentTYPE.HEADER.ordinal -> UpcomingContentListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            else -> UpcomignContentHeaderListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        }
        return UpcomingContentViewHolder(binding.root)
    }

    override fun getItem(position: Int): Any? {
        if (contents.size > position) return contents[position]
        return null
    }

    override fun getItemViewType(position: Int): Int {
        val content = getItem(position)
        return if (content is String){
            UpcomingContentTYPE.HEADER.ordinal
        } else {
            UpcomingContentTYPE.FOOTER.ordinal
        }
    }

    override fun onBindViewHolder(holder: UpcomingContentViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            holder.bind(content)
        }
    }
}

class UpcomingContentViewHolder(val view: View) :
    RecyclerView.ViewHolder(view) {
    private val title = binding.runningContentTitle
    private val path = binding.treePath
    private val date = binding.startDateAndEndDate
    private val image = binding.runningContentImage

    init {
        title.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
        path.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
        date.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
    }

    fun bind(content: Any) {
        if (content is String){
            bindHeader(content)
        } else {
            bindFooter(content as DomainContent)
        }

        title.text = content.title
        path.text = content.treePath
        date.text = content.getFormattedStartDateAndEndDate()
        image.setImageResource(setContentImage(content.contentType))
        itemView.setOnClickListener { clickListener(content) }
        setViewVisibility(content)
    }

    private fun bindHeader(date: String){

    }

    private fun bindFooter(content: DomainContent){

        val title = view.findViewById<TextView>(R.id.running_content_title)
        val path = binding.treePath
        val date = binding.startDateAndEndDate
        val image = binding.runningContentImage
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

    private fun setViewVisibility(content: DomainContent) {
        if (content.getFormattedStartDateAndEndDate() != "") {
            ViewUtils.setGone(date, false)
        }
    }
}

enum class UpcomingContentTYPE{
    HEADER,FOOTER
}