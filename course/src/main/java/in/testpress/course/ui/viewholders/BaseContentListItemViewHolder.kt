package `in`.testpress.course.ui.viewholders

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.models.greendao.ChapterDao
import `in`.testpress.models.greendao.CourseDao
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

abstract class BaseContentListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val title: TextView = view.findViewById(R.id.title)
    private val scheduledInfo: TextView = view.findViewById(R.id.scheduled_info)
    private val whiteForeground: View = view.findViewById(R.id.white_foreground)
    private val lockContainer: LinearLayout = view.findViewById(R.id.lock_container)
    private val attemptedTickContainer: LinearLayout = view.findViewById(R.id.attempted_tick_container)
    private val lockImage: ImageView = view.findViewById(R.id.lock_image)
    private val scheduledInfoContainer: LinearLayout = view.findViewById(R.id.scheduled_info_container)
    private val contentDetailsContainer: LinearLayout = view.findViewById(R.id.content_details_container)

    init {
        title.typeface = TestpressSdk.getRubikMediumFont(view.context)
        scheduledInfo.typeface = TestpressSdk.getRubikMediumFont(view.context)
    }

    fun bind(content: DomainContent, isPremium: Boolean, clickListener: (DomainContent) -> Unit) {
        title.text = content.title
        handleLockedAndScheduledContent(content)
        if (isPremium) showCrownIcon()
        bindContentDetails(content)
        itemView.setOnClickListener { clickListener(content)}
    }

    private fun showScheduledInfo(content: DomainContent) {
        if (content.getFormattedStart() != null) {
            scheduledInfo.text = "This will be available on " + content.getFormattedStart()
        } else {
            scheduledInfo.text = "Coming soon"
        }
        scheduledInfoContainer.visibility = View.VISIBLE
        contentDetailsContainer.visibility = View.GONE
    }

    private fun showLockIcon() {
        whiteForeground.visibility = View.VISIBLE
        lockContainer.visibility = View.VISIBLE
    }

    private fun showClockIcon() {
        showLockIcon()
        lockImage.setImageResource(R.drawable.clock)
    }

    private fun showCrownIcon() {
        showLockIcon()
        lockImage.setImageResource(R.drawable.crown)
    }

    private fun hideLockIcon() {
        whiteForeground.visibility = View.GONE
        lockContainer.visibility = View.GONE
    }

    private fun handleLockedAndScheduledContent(content: DomainContent) {
        scheduledInfoContainer.visibility = View.GONE
        attemptedTickContainer.visibility = View.GONE
        itemView.isClickable = false

        if (content.isScheduled == true) {
            showClockIcon()
            showScheduledInfo(content)
        } else if (content.isLocked == true) {
            showLockIcon()
        } else {
            hideLockIcon()
            if (content.hasAttempted()) {
                attemptedTickContainer.visibility = View.VISIBLE
            }
            itemView.isClickable = true
        }
    }

    abstract fun bindContentDetails(content: DomainContent)
}