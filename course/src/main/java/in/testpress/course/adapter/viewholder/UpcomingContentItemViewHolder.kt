package `in`.testpress.course.adapter.viewholder

import android.widget.Toast
import `in`.testpress.course.adapter.BaseCourseContentItemViewHolder
import `in`.testpress.course.databinding.RunningUpcomingListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.util.DateUtils
import `in`.testpress.util.ViewUtils

class UpcomingContentItemViewHolder(val binding: RunningUpcomingListItemBinding) :
    BaseCourseContentItemViewHolder(binding) {

    override fun bind(content: DomainContent) {
        super.bind(content)
        showOrHideDate(content)
        onItemClick(content)
    }

    private fun showOrHideDate(content: DomainContent) {
        binding.date.text = "Avaliable ${DateUtils.getRelativeTimeString(content.start, binding.root.context)} - "
        val visibility = DateUtils.getRelativeTimeString(content.start, binding.root.context).isEmpty()
        ViewUtils.setGone(binding.date, visibility)
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