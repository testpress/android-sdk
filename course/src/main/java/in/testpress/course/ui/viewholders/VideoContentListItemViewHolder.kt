package `in`.testpress.course.ui.viewholders

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainVideoContent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.testpress.mikephil.charting.charts.PieChart
import com.github.testpress.mikephil.charting.data.PieData
import com.github.testpress.mikephil.charting.data.PieDataSet
import com.github.testpress.mikephil.charting.data.PieEntry
import java.util.*

class VideoContentListItemViewHolder(view: View) : BaseContentListItemViewHolder(view) {
    private val duration: TextView = view.findViewById(R.id.duration)
    private val durationContainer: LinearLayout = view.findViewById(R.id.duration_container)
    private val videoCompletionProgressContainer: LinearLayout = view.findViewById(R.id.video_completion_progress_container)
    private var progressChart: PieChart = view.findViewById(R.id.video_completion_progress_chart)

    init {
        duration.typeface = TestpressSdk.getRubikMediumFont(view.context)
    }

    override fun bindContentDetails(content: DomainContent) {
        content.video?.let {
            bindDuration(it)
            bindVideoProgress(content)
        }
    }

    private fun bindVideoProgress(content: DomainContent) {
        attemptedTickContainer.visibility = View.GONE
        videoCompletionProgressContainer.visibility = View.GONE

        when (content.videoWatchedPercentage) {
            0, null -> {}
            100 ->  attemptedTickContainer.visibility = View.VISIBLE
            else -> showVideoProgressPercentage(content.videoWatchedPercentage)
        }
    }

    private fun showVideoProgressPercentage(videoWatchedPercentage: Int) {
        videoCompletionProgressContainer.visibility = View.VISIBLE
        progressChart.setDescription("")
        progressChart.isClickable = false
        progressChart.data = getVideoProgressPieChartData(videoWatchedPercentage)
        progressChart.setTouchEnabled(false)
        progressChart.setUsePercentValues(true)
        progressChart.centerText = "$videoWatchedPercentage%"
        progressChart.setCenterTextSize(7f)
        progressChart.setCenterTextTypeface(TestpressSdk.getRubikMediumFont(itemView.context))
        progressChart.setCenterTextColor(ContextCompat.getColor(itemView.context, R.color.testpress_text_gray))
        progressChart.holeRadius = 85f
        progressChart.transparentCircleRadius = 0f
        progressChart.setExtraOffsets(0f, 0f, 0f, 0f)
        progressChart.legend.isEnabled = false
    }

    private fun bindDuration(it: DomainVideoContent) {
        if (it.duration.isNullOrBlank()) {
            durationContainer.visibility = View.GONE
        } else {
            duration.text = it.duration
            durationContainer.visibility = View.VISIBLE
        }
    }

    private fun getVideoProgressPieChartData(watchedDuration: Int): PieData? {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(watchedDuration.toFloat(), 0))
        entries.add(PieEntry((100 - watchedDuration).toFloat(), 1))
        val dataSet = PieDataSet(entries, "")
        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(itemView.context, R.color.testpress_green))
        colors.add(ContextCompat.getColor(itemView.context, R.color.testpress_gray_light))
        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setDrawValues(false)
        return data
    }

    companion object {
        fun create(parent: ViewGroup): VideoContentListItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.video_content_list_item, parent, false)
            return VideoContentListItemViewHolder(view)
        }

    }
}