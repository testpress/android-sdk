package `in`.testpress.course.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.adapter.viewholder.RunningContentItemViewHolder
import `in`.testpress.course.adapter.viewholder.UpcomingContentItemViewHolder
import `in`.testpress.course.databinding.RunningUpcomingListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.database.entities.ContentEntityLite
import `in`.testpress.database.entities.CourseContentType

class CourseContentListAdapter :
    PagingDataAdapter<ContentEntityLite, BaseCourseContentItemViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseCourseContentItemViewHolder {
        val binding = RunningUpcomingListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return if (viewType == CourseContentType.RUNNING_CONTENT.ordinal) {
            RunningContentItemViewHolder(binding)
        } else {
            UpcomingContentItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: BaseCourseContentItemViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            val domainContent = content.asDomainContent()
            holder.bind(domainContent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val content = getItem(position)
        return when(content?.type){
            CourseContentType.UPCOMING_CONTENT.ordinal -> CourseContentType.UPCOMING_CONTENT.ordinal
            else -> CourseContentType.RUNNING_CONTENT.ordinal
        }
    }

    companion object {
        private val COMPARATOR =
            object : DiffUtil.ItemCallback<ContentEntityLite>() {
                override fun areContentsTheSame(
                    oldItem: ContentEntityLite,
                    newItem: ContentEntityLite
                ): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: ContentEntityLite,
                    newItem: ContentEntityLite
                ): Boolean =
                    oldItem.id == newItem.id
            }
    }

}

open class BaseCourseContentItemViewHolder(binding: RunningUpcomingListItemBinding) :
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

    open fun bind(content: DomainContent) {
        showContentDetails(content)
    }

    private fun showContentDetails(content: DomainContent){
        title.text = content.title
        path.text = "${content.treePath}"
        thumbnail.setImageResource(getContentImage(content.contentType))
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