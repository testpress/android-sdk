package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.NetworkForum
import `in`.testpress.ui.view.RoundedImageView
import `in`.testpress.util.FormatDate
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.ViewUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import java.text.ParseException
import java.text.SimpleDateFormat


open class DiscussionsAdapter(private val onItemClicked: (NetworkForum) -> Unit) :
        PagingDataAdapter<NetworkForum, DiscussionViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forum_list_item, parent, false)
        return DiscussionViewHolder(view) {
            Log.d("TAG", "onCreateViewHolder: ${getItem(it)?.title}")
            onItemClicked(getItem(it)!!)
        }
    }

    override fun onBindViewHolder(holder: DiscussionViewHolder, position: Int) {
        getItem(position)?.let { holder.bindPost(it) }
    }

    companion object {
        const val TAG = "DownloadsAdapter"

        private val COMPARATOR = object : DiffUtil.ItemCallback<NetworkForum>() {
            override fun  areItemsTheSame(oldItem: NetworkForum, newItem: NetworkForum): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: NetworkForum, newItem: NetworkForum): Boolean = oldItem.contentHtml == newItem.contentHtml
                    && oldItem.commentsCount == newItem.commentsCount
        }
    }
}


class DiscussionViewHolder(itemView: View, onItemClicked: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
    private val titleText: TextView = itemView.findViewById(R.id.title)
    private val date: TextView = itemView.findViewById(R.id.date)
    private val viewsCountView: TextView = itemView.findViewById(R.id.viewsCount)
    private val statusText: TextView = itemView.findViewById(R.id.status)
    var roundedImageView: RoundedImageView = itemView.findViewById(R.id.display_picture)
    var imageLoader: ImageLoader
    val imageOptions: DisplayImageOptions

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    init {
        simpleDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
        imageLoader = ImageUtils.initImageLoader(itemView.context)
        imageOptions = ImageUtils.getAvatarPlaceholdersOption()
        itemView.setOnClickListener {
            Log.d("TAG", "Viewholder: ")
            onItemClicked(bindingAdapterPosition)
        }
    }

    fun bindPost(forumPost: NetworkForum) {
        with(forumPost) {
            titleText.text = title
            date.text = FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(created).time)
            viewsCountView.text = viewsCount.toString() + " views"
            ViewUtils.setTypeface(arrayOf(titleText, statusText), TestpressSdk.getRubikMediumFont(itemView.context))
            ViewUtils.setTypeface(arrayOf(date, viewsCountView), TestpressSdk.getRubikRegularFont(itemView.context))

            imageLoader.displayImage(createdBy?.mediumImage, roundedImageView, imageOptions)

            if (commentsCount === 0 || lastCommentedBy == null) {
                try {
                    statusText.text = (createdBy?.displayName.toString() + " started " +
                            FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(lastCommentedTime).time))
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    statusText.text = (lastCommentedBy.displayName.toString() + " replied " +
                            FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(lastCommentedTime).time))
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
