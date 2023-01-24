package `in`.testpress.course.adapter

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.databinding.ContentStateListItemV2Binding
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.DateUtils
import `in`.testpress.util.ViewUtils
import android.content.Context
import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.Direction.NEXT
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit.*
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RunningContentAdapter(private val fragmentTag: String) :
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
    private val path = binding.contentPath
    private val date = binding.endDate
    private val thumbnail = binding.thumbnail

    init {
        title.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
        path.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
        date.typeface = TestpressSdk.getRubikMediumFont(binding.root.context)
    }

    fun bind(content: DomainContent, clickListener: (DomainContent) -> Unit) {
        title.text = content.title
        path.text ="> ${content.treePath}"
        thumbnail.setImageResource(getContentImage(content.contentType))
        itemView.setOnClickListener { clickListener(content) }
        val dateMessage = getDateMessage(content)
        date.text = dateMessage.toString()
        setDateViewVisibility(dateMessage)
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

        val r : RelativeDateTimeFormatter

        val currentDate = Date()
        val endDate = content.end
        val hoursDifference = getDateDifferentInHours(getDate(endDate),currentDate)
        return getValidationString(hoursDifference)
    }

    private fun getDate(dateString: String?): Date? {
        var date: Date? = null
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        if (dateString != null && dateString != "") {
            date = try {
                dateString.let { simpleDateFormat.parse(it) }
            } catch (e: Exception) {
                null
            }
        }
        return date
    }

    private fun getDateDifferentInHours(date1: Date?, date2: Date?): Long? {
        if (date1 == null || date2 == null) {
            return null
        }
        val diff: Long = date1.time - date2.time
        return ((diff / 1000L) / 60L) / 60L
    }

    private fun getValidationString(hours: Long?): String? {
        val fmt: RelativeDateTimeFormatter = RelativeDateTimeFormatter.getInstance()

        return if (hours == null){
            null
        } else if (hours > ONE_YEAR_IN_HOUR){
            fmt.format((((hours/24)/30)/12).toDouble(), NEXT, YEARS)
        } else if (hours > ONE_MONTH_IN_HOUR){
            fmt.format(((hours/24)/30).toDouble(), NEXT, MONTHS)
        } else if (hours > ONE_DAY_IN_HOUR) {
            fmt.format((hours/24).toDouble(), NEXT, DAYS)
        } else {
            fmt.format(hours.toDouble(), NEXT, HOURS)
        }
    }
}

private const val ONE_YEAR_IN_HOUR = 8640
private const val ONE_MONTH_IN_HOUR = 720
private const val ONE_DAY_IN_HOUR = 24