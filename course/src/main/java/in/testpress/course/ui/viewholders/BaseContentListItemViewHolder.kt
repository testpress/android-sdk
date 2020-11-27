package `in`.testpress.course.ui.viewholders

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.util.ImageUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader

abstract class BaseContentListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val title: TextView = view.findViewById(R.id.title)
    private val thumbnail: ImageView = view.findViewById(R.id.thumbnail)
    private val scheduledInfo: TextView = view.findViewById(R.id.scheduled_info)
    private val lockContainer: LinearLayout = view.findViewById(R.id.lock_container)
    val attemptedTickContainer: LinearLayout = view.findViewById(R.id.attempted_tick_container)
    private val lockImage: ImageView = view.findViewById(R.id.lock_image)
    private val scheduledInfoContainer: LinearLayout = view.findViewById(R.id.scheduled_info_container)
    private val contentDetailsContainer: LinearLayout = view.findViewById(R.id.content_details_container)
    val contentTypeIcon: ImageView = view.findViewById(R.id.content_type_icon)
    private var imageLoader: ImageLoader = ImageUtils.initImageLoader(view.context)
    private var imageOptions: DisplayImageOptions = ImageUtils.getPlaceholdersOption()

    init {
        title.typeface = TestpressSdk.getRubikMediumFont(view.context)
        scheduledInfo.typeface = TestpressSdk.getRubikMediumFont(view.context)
    }

    fun bind(content: DomainContent, isPremium: Boolean, clickListener: (DomainContent) -> Unit) {
        title.text = content.title
        updateThumbnail(content)
        handleLockedAndScheduledContent(content)
        if (isPremium) showCrownIcon()
        bindContentDetails(content)
        itemView.setOnClickListener { clickListener(content)}
    }

    private fun updateThumbnail(content: DomainContent) {
        if (!content.coverImageMedium.isNullOrEmpty()) {
            imageLoader.displayImage(content.coverImageMedium, thumbnail, imageOptions)
        }
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

    open fun bindContentDetails(content: DomainContent) {}
}