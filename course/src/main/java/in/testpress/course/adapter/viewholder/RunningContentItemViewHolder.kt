package `in`.testpress.course.adapter.viewholder

import `in`.testpress.course.adapter.BaseCourseContentItemViewHolder
import `in`.testpress.course.databinding.RunningUpcomingListItemBinding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.util.getActivity

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
            val context = binding.root.context
            val activity = context.getActivity()
            val enableLocalFileDownload = activity?.intent?.getBooleanExtra(TestpressCourse.ENABLE_LOCAL_FILE_DOWNLOAD, true) ?: true
            context.startActivity(
                ContentActivity.createIntent(
                    content.id,
                    context,
                    "",
                    false,
                    false,
                    enableLocalFileDownload
                )
            )
        }
    }
}