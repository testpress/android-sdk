package `in`.testpress.course.adapter.viewholder

import `in`.testpress.course.adapter.BaseCourseContentItemViewHolder
import `in`.testpress.course.databinding.RunningUpcomingListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.util.DateUtils.convertDateFormat

class RunningContentItemViewHolder(val binding: RunningUpcomingListItemBinding) :
    BaseCourseContentItemViewHolder(binding) {

    override fun bind(content: DomainContent) {
        super.bind(content)
        setDateText(content)
        onItemClick(content)
    }

    private fun setDateText(content: DomainContent) {
        val startDate = convertDateFormat(content.start)
        val endDate = content.end?.let { " - ${convertDateFormat(it)}" } ?: ""
        binding.date.text = "$startDate$endDate"
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