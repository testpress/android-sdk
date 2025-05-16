package `in`.testpress.course.adapter.viewholder

import `in`.testpress.course.adapter.BaseCourseContentItemViewHolder
import `in`.testpress.course.databinding.RunningUpcomingListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity

class RunningContentItemViewHolder(val binding: RunningUpcomingListItemBinding) :
    BaseCourseContentItemViewHolder(binding) {

    override fun bind(content: DomainContent) {
        onItemClick(content)
        binding.composeView.setContent {
            RunningContentItemView(content)
        }
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