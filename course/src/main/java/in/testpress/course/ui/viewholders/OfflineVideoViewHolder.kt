package `in`.testpress.course.ui.viewholders

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.course.domain.DomainOfflineVideo
import `in`.testpress.course.ui.DownloadedVideoClickListener
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class OfflineVideoViewHolder(val view: View) : RecyclerView.ViewHolder(view),
    PopupMenu.OnMenuItemClickListener {
    private val image: ImageView = view.findViewById(R.id.image)
    private val title: TextView = view.findViewById(R.id.title)
    private val size: TextView = view.findViewById(R.id.size)
    private val duration: TextView = view.findViewById(R.id.duration)
    private val menuButton: ImageButton = view.findViewById(R.id.menu_button)
    private val percentageDownloadedLayout: LinearLayout =
        view.findViewById(R.id.percentage_downloaded_layout)
    private val percentageDownloadedProgress: ProgressBar =
        view.findViewById(R.id.percentage_downloaded_progress)
    private val percentageDownloaded: TextView = view.findViewById(R.id.percentage_downloaded)
    private val buttonsLayout: LinearLayout = view.findViewById(R.id.buttons_layout)
    private val pauseButton: MaterialButton = view.findViewById(R.id.pause_button)
    private val cancelButton: MaterialButton = view.findViewById(R.id.cancel_button)
    private var eventListener: DownloadedVideoClickListener? = null
    private lateinit var offlineVideo: DomainOfflineVideo

    init {
        title.typeface = TestpressSdk.getRubikMediumFont(view.context)
    }

    fun bind(offlineVideo: DomainOfflineVideo, clickListener: DownloadedVideoClickListener) {
        this.offlineVideo = offlineVideo
        this.eventListener = clickListener
        bindViews()
        initializeListeners()
    }

    private fun bindViews() {
        title.text = offlineVideo.title
        duration.text = offlineVideo.duration
        showVideoSize()

        if (offlineVideo.isDownloadCompleted) {
            hideDownloadProgress()
            hidePauseCancelButtons()
        } else {
            showDownloadProgress()
            showPauseCancelButtons()
        }
    }

    private fun showDownloadProgress() {
        percentageDownloadedLayout.visibility = View.VISIBLE
        percentageDownloadedProgress.progress = offlineVideo.percentageDownloaded
        percentageDownloaded.text = "${offlineVideo.percentageDownloaded} %"
    }

    private fun hideDownloadProgress() {
        percentageDownloadedLayout.visibility = View.GONE
    }

    private fun showPauseCancelButtons() {
        buttonsLayout.visibility = View.VISIBLE
    }

    private fun hidePauseCancelButtons() {
        buttonsLayout.visibility = View.GONE
    }

    private fun showVideoSize() {
        if (offlineVideo.bytesDownloaded != 0L) {
            size.text = "${(offlineVideo.bytesDownloaded / 1000000)} MB"
        } else {
            size.text = "0 MB"
        }
    }

    private fun initializeListeners() {
        title.setOnClickListener {
            eventListener?.onClick(offlineVideo)
        }
        image.setOnClickListener {
            eventListener?.onClick(offlineVideo)
        }
        pauseButton.setOnClickListener {
            handlePauseOrResumeClick(it.context)
        }
        cancelButton.setOnClickListener {
            eventListener?.onDelete(offlineVideo)
        }
        menuButton.setOnClickListener {
            showPopupMenu(it)
        }
    }

    private fun handlePauseOrResumeClick(context: Context) {
        if (pauseButton.text.toString().equals("Pause", ignoreCase = false)) {
            eventListener?.onPause(offlineVideo)
            pauseButton.text = "Resume"
            pauseButton.icon =
                ContextCompat.getDrawable(context, R.drawable.ic_baseline_play_arrow_24)
        } else {
            pauseButton.text = "Pause"
            pauseButton.icon =
                ContextCompat.getDrawable(context, R.drawable.ic_baseline_pause_24)
            eventListener?.onResume(offlineVideo)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                eventListener?.onDelete(offlineVideo)
                true
            }
            else -> false
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(view.context, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.downloaded_video_menu, popup.menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    companion object {
        const val TAG = "DownloadViewHolder"
        fun create(parent: ViewGroup): OfflineVideoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.downloaded_video_list_item, parent, false)
            return OfflineVideoViewHolder(view)
        }
    }
}
