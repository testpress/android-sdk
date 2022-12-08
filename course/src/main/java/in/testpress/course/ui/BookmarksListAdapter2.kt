package `in`.testpress.course.ui

import `in`.testpress.core.TestpressSdk
import `in`.testpress.course.R
import `in`.testpress.models.greendao.Bookmark
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ReviewItem
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class BookmarksListAdapter2(
    val activity: FragmentActivity,
    var bookmarks: List<Bookmark>,
    val folderName: String?
) : ListAdapter<Bookmark, BookmarksListAdapter2.BookmarkListItemViewHolder>(BOOKMARK_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookmarkListItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.testpress_bookmark_panel_list_item, parent, false)
        return BookmarkListItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bookmarks.size
    }

    override fun getItem(position: Int): Bookmark? {
        if (bookmarks.size > position) return bookmarks[position]
        return null
    }

    override fun onBindViewHolder(holder: BookmarkListItemViewHolder, position: Int) {
        val bookmark = getItem(position)
        if (bookmark != null) {
            holder.bind(bookmark) { onItemClick(it) }
        }
    }

    private fun onItemClick(bookmark: Bookmark) {
        val intent = Intent(activity, BookmarkDetailActivity::class.java)
        intent.putExtra("BOOKMARKID", bookmark.id)
        activity.startActivity(intent)
    }

    companion object {
        private val BOOKMARK_COMPARATOR = object : DiffUtil.ItemCallback<Bookmark>() {
            override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean =
                oldItem.id == newItem.id
        }
    }

    class BookmarkListItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val title: TextView = view.findViewById(R.id.content)
        private val thumbnail: ImageView = view.findViewById(R.id.thumbnail_image)

        init {
            title.typeface = TestpressSdk.getRubikRegularFont(view.context)
        }

        fun bind(bookmark: Bookmark, clickListener: (Bookmark) -> Unit) {

            val bookmarkType = bookmark.bookmarkedObject
            if (bookmarkType is ReviewItem) {
                val reviewItem: ReviewItem = bookmarkType
                title.setText(Html.fromHtml(reviewItem.getQuestion().getQuestionHtml())
                    .toString()
                    .trim { it <= ' ' })
                thumbnail.setImageResource(R.drawable.testpress_question_content_icon)
            } else if (bookmarkType is Content) {
                val content: Content = bookmarkType
                title.setText(content.name)
                if (content.htmlId != null) {
                    thumbnail.setImageResource(R.drawable.testpress_ebook_content_icon)
                } else if (content.videoId != null) {
                    thumbnail.setImageResource(R.drawable.testpress_video_content_icon)
                } else if (content.attachmentId != null) {
                    thumbnail.setImageResource(R.drawable.testpress_attachment_content_icon)
                }
            }
            itemView.setOnClickListener { clickListener(bookmark) }
        }
    }

}