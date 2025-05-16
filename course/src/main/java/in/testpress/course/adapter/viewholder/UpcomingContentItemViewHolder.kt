package `in`.testpress.course.adapter.viewholder

import android.widget.Toast
import `in`.testpress.course.adapter.BaseCourseContentItemViewHolder
import `in`.testpress.course.databinding.RunningUpcomingListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.util.DateUtils

class UpcomingContentItemViewHolder(val binding: RunningUpcomingListItemBinding) :
    BaseCourseContentItemViewHolder(binding) {

    override fun bind(content: DomainContent) {
        onItemClick(content)
        binding.composeView.setContent {
            UpcomingContentItemView(content)
        }
    }

    private fun onItemClick(content: DomainContent) {
        itemView.setOnClickListener {
            Toast.makeText(
                binding.root.context,
                "Avaliable ${DateUtils.getRelativeTimeString(content.start, binding.root.context)}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}