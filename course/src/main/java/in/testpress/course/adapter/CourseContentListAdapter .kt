package `in`.testpress.course.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
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

abstract class BaseCourseContentItemViewHolder(binding: RunningUpcomingListItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(content: DomainContent)
}