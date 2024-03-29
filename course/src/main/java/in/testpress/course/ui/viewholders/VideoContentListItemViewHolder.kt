package `in`.testpress.course.ui.viewholders

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.DomainVideoContent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible

class VideoContentListItemViewHolder(view: View) : BaseContentListItemViewHolder(view) {
    private val duration: TextView = view.findViewById(R.id.duration)
    private val durationContainer: LinearLayout = view.findViewById(R.id.duration_container)
    private val videoCompletionProgressContainer: FrameLayout = view.findViewById(R.id.video_completion_progress_container)
    private var progressbar: ProgressBar = view.findViewById(R.id.video_completion_progress)

    init {
        duration.typeface = TestpressSdk.getRubikMediumFont(view.context)
    }

    override fun bindContentDetails(content: DomainContent) {
        content.video?.let {
            bindDuration(it)
            bindVideoProgress(content)
        }?: run {
            hideVideoDuration()
        }
        contentTypeIcon.visibility = View.VISIBLE
    }

    private fun hideVideoDuration(){
        durationContainer.visibility = View.GONE
    }

    private fun bindVideoProgress(content: DomainContent) {
        attemptedTickContainer.isVisible = content.hasAttempted()
        videoCompletionProgressContainer.visibility = View.GONE
        content.videoWatchedPercentage?.let { watchedPercentage ->
            if (watchedPercentage > 0) {
                showVideoProgress(watchedPercentage)
            }
        }
    }

    private fun showVideoProgress(videoWatchedPercentage: Int) {
        videoCompletionProgressContainer.visibility = View.VISIBLE
        progressbar.progress = videoWatchedPercentage
    }

    private fun bindDuration(it: DomainVideoContent) {
        if (it.duration.isNullOrBlank()) {
            durationContainer.visibility = View.GONE
        } else {
            duration.text = it.duration
            durationContainer.visibility = View.VISIBLE
        }
    }

    companion object {
        fun create(parent: ViewGroup): VideoContentListItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.video_content_list_item, parent, false)
            return VideoContentListItemViewHolder(view)
        }

    }
}