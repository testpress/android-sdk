package `in`.testpress.course.adapter

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.DateUtils
import `in`.testpress.database.entities.RunningContentEntity
import android.content.Context
import androidx.recyclerview.widget.DiffUtil

class RunningContentListAdapter:BaseContentListAdapter<RunningContentEntity>(COMPARATOR){

    companion object {
        private val COMPARATOR =
            object : DiffUtil.ItemCallback<RunningContentEntity>() {
                override fun areContentsTheSame(
                    oldItem: RunningContentEntity,
                    newItem: RunningContentEntity
                ): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: RunningContentEntity,
                    newItem: RunningContentEntity
                ): Boolean =
                    oldItem.id == newItem.id
            }
    }

    override fun onItemClick(content: DomainContent, context: Context) {
        context.startActivity(
            ContentActivity.createIntent(
                content.id,
                context,
                ""
            )
        )
    }

    override fun getDateText(content: DomainContent, context: Context):
            String = "Ends ${DateUtils.getHumanizedDateFormatOrEmpty(content.end, context)} - "

    override fun getDateVisiblity(content: DomainContent, context: Context):
            Boolean = DateUtils.getHumanizedDateFormatOrEmpty(content.end, context).isEmpty()

}