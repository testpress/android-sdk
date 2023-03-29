package `in`.testpress.course.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.databinding.RunningUpcomingListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.util.ViewUtils

abstract class BaseContentListAdapter<T : Any>(COMPARATOR: DiffUtil.ItemCallback<T>):
    PagingDataAdapter<T, BaseContentListViewHolder>(COMPARATOR){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseContentListViewHolder {
        val binding = RunningUpcomingListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BaseContentListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseContentListViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            val domainContent = content.asDomainContent()
            holder.bind(domainContent) { onItemClick(domainContent, holder.itemView.context) }
            holder.setDate(getDateText(domainContent, holder.itemView.context))
            holder.setDateVisiblity(getDateVisiblity(domainContent, holder.itemView.context))
        }
    }

    abstract fun onItemClick(content: DomainContent, context: Context)

    abstract fun getDateText(content: DomainContent, context: Context):String

    abstract fun getDateVisiblity(content: DomainContent, context: Context):Boolean

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
    }

    fun setDate(dateText: String) {
        date.text = dateText
    }

    fun setDateVisiblity(visibility: Boolean) {
        ViewUtils.setGone(date, visibility)
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
}