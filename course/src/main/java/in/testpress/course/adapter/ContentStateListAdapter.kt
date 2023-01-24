package `in`.testpress.course.adapter

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.databinding.ContentStateListItemV2Binding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.DateUtils
import `in`.testpress.course.util.DateUtils.ONE_DAY_IN_HOUR
import `in`.testpress.course.util.DateUtils.ONE_MONTH_IN_HOUR
import `in`.testpress.course.util.DateUtils.ONE_YEAR_IN_HOUR
import `in`.testpress.util.ViewUtils
import android.content.Context
import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.Direction.NEXT
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class ContentStateListAdapter(private val fragmentTag: String) :
    ListAdapter<DomainContent, RunningContentViewHolder>(DOMAIN_CONTENT_COMPARATOR) {

    var contents: List<DomainContent> = listOf()

    companion object {
        private val DOMAIN_CONTENT_COMPARATOR =
            object : DiffUtil.ItemCallback<DomainContent>() {
                override fun areContentsTheSame(
                    oldItem: DomainContent,
                    newItem: DomainContent
                ): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: DomainContent,
                    newItem: DomainContent
                ): Boolean =
                    oldItem.id == newItem.id
            }
    }

    override fun getItemCount(): Int {
        return contents.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RunningContentViewHolder {
        val binding = ContentStateListItemV2Binding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RunningContentViewHolder(binding)
    }

    private fun onItemClick(content: DomainContent, context: Context) {
        if (fragmentTag == "Upcoming" && DateUtils.getFormattedStartDate(content.start) != ""){
            Toast.makeText(context,"This will be available ${DateUtils.getFormattedStartDate(content.start)}",
                Toast.LENGTH_SHORT).show()
        } else if (fragmentTag == "Upcoming") {
            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
        } else{
            context.startActivity(
                ContentActivity.createIntent(
                    content.id,
                    context,
                    ""
                )
            )
        }
    }

    override fun getItem(position: Int): DomainContent? {
        if (contents.size > position) return contents[position]
        return null
    }

    override fun onBindViewHolder(holder: RunningContentViewHolder, position: Int) {
        val content = getItem(position)
        if (content != null) {
            holder.bind(content) { onItemClick(content, holder.itemView.context) }
        }
    }
}

class RunningContentViewHolder(binding: ContentStateListItemV2Binding) :
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

    fun bind(content: DomainContent, clickListener: (DomainContent) -> Unit) {
        title.text = content.title
        path.text ="${content.treePath}"
        date.text = "Ends ${getDateMessage(content)} - "
        thumbnail.setImageResource(getContentImage(content.contentType))
        itemView.setOnClickListener { clickListener(content) }
        setDateViewVisibility(getDateMessage(content))
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

    private fun setDateViewVisibility(dateText: String?){
        if (dateText == null || dateText == "") {
            ViewUtils.setGone(date,true)
        }
    }

    private fun getDateMessage(content: DomainContent):String? {
        val currentDate = Date()
        val endDate = DateUtils.convertDateStringToDate(content.end)
        val hoursDifference = DateUtils.getDateDifferentInHours(endDate,currentDate)
        return getValidationString(hoursDifference)
    }

    private fun getValidationString(hours: Long?): String? {
        val fmt: RelativeDateTimeFormatter = RelativeDateTimeFormatter.getInstance()
        return when {
            hours == null -> null
            hours > ONE_YEAR_IN_HOUR -> fmt.format((((hours/24)/30)/12).toDouble(), NEXT, YEARS)    // output in 1 year
            hours > ONE_MONTH_IN_HOUR -> fmt.format(((hours/24)/30).toDouble(), NEXT, MONTHS)       // output in 2 months
            hours > ONE_DAY_IN_HOUR -> fmt.format((hours/24).toDouble(), NEXT, DAYS)                // output in 5 days
            else -> fmt.format(hours.toDouble(), NEXT, HOURS)                                       // output in 10 hours
        }
    }
}