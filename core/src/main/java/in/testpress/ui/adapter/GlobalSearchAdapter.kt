package `in`.testpress.ui.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.databinding.SearchResultItemBinding
import `in`.testpress.models.SearchResult

class GlobalSearchAdapter :
    PagingDataAdapter<SearchResult, GlobalSearchAdapter.SearchResultHolder>(ARTICLE_DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultHolder =
        SearchResultHolder(
            SearchResultItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )

    override fun onBindViewHolder(holder: SearchResultHolder, position: Int) {
        val tile = getItem(position)
        if (tile != null) {
            holder.bind(tile)
        }
    }

    class SearchResultHolder(
        private val binding: SearchResultItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(searchResult: SearchResult) {
            binding.apply {
                val title = convertHighlightToInlineStyle(searchResult.highlight?.title ?: "")
                binding.title.text = Html.fromHtml(title)
                binding.type.text = searchResult.type
                binding.active.text = searchResult.active.toString()
            }
        }

        private fun convertHighlightToInlineStyle(htmlResponse: String): String {
            return htmlResponse.replace(
                "class=\'highlight\'",
                "style=\'background-color: yellow;\'"
            )
        }
    }

    companion object {
        private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<SearchResult>() {
            override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult): Boolean =
                oldItem == newItem
        }
    }
}
