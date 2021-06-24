package `in`.testpress.ui

import `in`.testpress.R
import `in`.testpress.database.entities.DiscussionPostEntity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil


open class DiscussionsAdapter(private val onItemClicked: (DiscussionPostEntity) -> Unit) :
        PagingDataAdapter<DiscussionPostEntity, DiscussionViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forum_list_item, parent, false)
        return DiscussionViewHolder(view) {
            onItemClicked(getItem(it)!!)
        }
    }

    override fun onBindViewHolder(holder: DiscussionViewHolder, position: Int) {
        getItem(position)?.let { holder.bindPost(it) }
    }

    companion object {

        private val COMPARATOR = object : DiffUtil.ItemCallback<DiscussionPostEntity>() {
            override fun  areItemsTheSame(oldItem: DiscussionPostEntity, newItem: DiscussionPostEntity): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: DiscussionPostEntity, newItem: DiscussionPostEntity): Boolean = oldItem.contentHtml == newItem.contentHtml
                    && oldItem.commentsCount == newItem.commentsCount
        }
    }
}