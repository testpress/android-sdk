package `in`.testpress.course.adapter.viewholder

import `in`.testpress.course.adapter.BaseCourseContentItemViewHolder
import `in`.testpress.course.databinding.RunningUpcomingListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.util.DateUtils
import `in`.testpress.util.ViewUtils

class RunningContentItemViewHolder(val binding: RunningUpcomingListItemBinding) :
    BaseCourseContentItemViewHolder(binding) {

    override fun bind(content: DomainContent) {
        super.bind(content)
        showOrHideDate(content)
        onItemClick(content)
    }

    private fun showOrHideDate(content: DomainContent) {
        binding.date.text =
            "Ends ${DateUtils.getRelativeTimeString(content.end, binding.root.context)} - "
        val visibility =
            DateUtils.getRelativeTimeString(content.end, binding.root.context).isEmpty()
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