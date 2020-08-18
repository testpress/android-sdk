package `in`.testpress.course.ui

import `in`.testpress.course.domain.DomainOfflineVideo
import `in`.testpress.course.helpers.DownloadTask
import `in`.testpress.course.ui.ContentActivity.HIDE_BOTTOM_NAVIGATION
import `in`.testpress.course.ui.viewholders.OfflineVideoViewHolder
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class OfflineVideoListAdapter :
    ListAdapter<DomainOfflineVideo, OfflineVideoViewHolder>(DOWNLOAD_COMPARATOR) {
    var offlineVideos: List<DomainOfflineVideo> = emptyList()

    override fun getItemCount(): Int {
        return offlineVideos.count()
    }

    override fun getItem(position: Int): DomainOfflineVideo? {
        return if (offlineVideos.count() > position) offlineVideos[position] else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineVideoViewHolder {
        return OfflineVideoViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: OfflineVideoViewHolder, position: Int) {
        val offlineVideo = getItem(position)
        val listener = downloadsListener(holder.view.context)

        offlineVideo?.let {
            holder.bind(offlineVideo, listener)
        }
    }

    private fun downloadsListener(context: Context): DownloadedVideoClickListener {
        return object : DownloadedVideoClickListener {
            override fun onDelete(offlineVideo: DomainOfflineVideo) {
                DownloadTask(offlineVideo.url!!, context).delete()
            }

            override fun onClick(offlineVideo: DomainOfflineVideo) {
                val intent = ContentActivity.createIntent(offlineVideo.contentId, context, null)
                intent.putExtra(HIDE_BOTTOM_NAVIGATION, true)
                context.startActivity(intent)
            }

            override fun onPause(offlineVideo: DomainOfflineVideo) {
                DownloadTask(offlineVideo.url!!, context).pause()
            }

            override fun onResume(offlineVideo: DomainOfflineVideo) {
                DownloadTask(offlineVideo.url!!, context).resume()
            }
        }
    }

    companion object {
        const val TAG = "DownloadsAdapter"

        private val DOWNLOAD_COMPARATOR = object : DiffUtil.ItemCallback<DomainOfflineVideo>() {
            override fun areItemsTheSame(
                oldItem: DomainOfflineVideo,
                newItem: DomainOfflineVideo
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: DomainOfflineVideo,
                newItem: DomainOfflineVideo
            ): Boolean = oldItem.url == newItem.url
        }
    }
}

interface DownloadedVideoClickListener {
    fun onDelete(offlineVideo: DomainOfflineVideo)
    fun onClick(offlineVideo: DomainOfflineVideo)
    fun onPause(offlineVideo: DomainOfflineVideo)
    fun onResume(offlineVideo: DomainOfflineVideo)
}