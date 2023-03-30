package `in`.testpress.course.adapter

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
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.DateUtils
import `in`.testpress.util.ViewUtils

class BaseContentListAdapter<T : Any>(COMPARATOR: DiffUtil.ItemCallback<T>):
    PagingDataAdapter<T, BaseContentListViewHolder>(COMPARATOR){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseContentListViewHolder {
        val binding = RunningUpcomingListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RunningContentListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseContentListViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            val domainContent = content.asDomainContent()
            holder.bind(domainContent)
        }
    }
}

open class BaseContentListViewHolder(binding: RunningUpcomingListItemBinding) :
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

class RunningContentListViewHolder(val binding: RunningUpcomingListItemBinding):BaseContentListViewHolder(binding){

    override fun bind(content: DomainContent) {
        super.bind(content)
        showOrHideDate(content)
        onItemClick(content)
    }

    private fun showOrHideDate(content: DomainContent) {
        binding.date.text = "Ends ${DateUtils.getRelativeTimeString(content.end, binding.root.context)} - "
        val visibility = DateUtils.getRelativeTimeString(content.end, binding.root.context).isEmpty()
        ViewUtils.setGone(binding.date, visibility)
    }

    private fun onItemClick(content: DomainContent) {
        itemView.setOnClickListener {
            binding.root.context.startActivity(
                ContentActivity.createIntent(
                    content.id,
                    binding.root.context,
                    ""
                )
            )
        }
    }
}