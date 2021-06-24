package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.core.TestpressSdk
import `in`.testpress.database.entities.DiscussionPostEntity
import `in`.testpress.database.entities.UserEntity
import `in`.testpress.ui.view.RoundedImageView
import `in`.testpress.util.FormatDate
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.ViewUtils
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import java.text.ParseException
import java.text.SimpleDateFormat

class DiscussionViewHolder(itemView: View, onItemClicked: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
    private val titleText: TextView = itemView.findViewById(R.id.title)
    private val date: TextView = itemView.findViewById(R.id.date)
    private val viewsCountView: TextView = itemView.findViewById(R.id.viewsCount)
    private val statusText: TextView = itemView.findViewById(R.id.status)
    private var roundedImageView: RoundedImageView = itemView.findViewById(R.id.display_picture)

    var imageLoader: ImageLoader
    val imageOptions: DisplayImageOptions
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

    init {
        simpleDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
        imageLoader = ImageUtils.initImageLoader(itemView.context)
        imageOptions = ImageUtils.getAvatarPlaceholdersOption()
        itemView.setOnClickListener {
            onItemClicked(bindingAdapterPosition)
        }
    }

    fun bindPost(forumPost: DiscussionPostEntity) {
        with(forumPost) {
            titleText.text = title
            date.text = FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(created).time)
            viewsCountView.text = viewsCount.toString() + " views"
            imageLoader.displayImage(createdBy?.mediumImage, roundedImageView, imageOptions)

            if (commentsCount === 0 || lastCommentedBy == null) {
                showCreatedBy(createdBy, lastCommentedTime)
            } else {
                showLastCommentedBy(lastCommentedBy, lastCommentedTime)
            }

            ViewUtils.setTypeface(arrayOf(titleText, statusText), TestpressSdk.getRubikMediumFont(itemView.context))
            ViewUtils.setTypeface(arrayOf(date, viewsCountView), TestpressSdk.getRubikRegularFont(itemView.context))
        }
    }

    private fun showLastCommentedBy(lastCommentedBy: UserEntity, lastCommentedTime: String?) {
        try {
            statusText.text = (lastCommentedBy.displayName.toString() + " replied " +
                    FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(lastCommentedTime).time))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun showCreatedBy(createdBy: UserEntity?, lastCommentedTime: String?) {
        try {
            statusText.text = (createdBy?.displayName.toString() + " started " +
                    FormatDate.getAbbreviatedTimeSpan(simpleDateFormat.parse(lastCommentedTime).time))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
}
