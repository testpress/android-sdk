package `in`.testpress.store.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.database.entities.ProductCategoryEntity
import `in`.testpress.store.databinding.HorizontalListViewFooterLoadingBinding
import `in`.testpress.store.databinding.ListViewFooterLoadingBinding
import `in`.testpress.store.databinding.ProductCategoriesListItemBinding

class ProductCategoryAdapter(
    context: Context,
    private val onRetry: () -> Unit
) : ListAdapter<ProductCategoryEntity, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var footerState = FooterState.HIDDEN

    override fun getItemViewType(position: Int): Int {
        return if (position == currentList.size) VIEW_TYPE_FOOTER else VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return currentList.size + if (footerState == FooterState.HIDDEN) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VIEW_TYPE_FOOTER -> {
                val binding = HorizontalListViewFooterLoadingBinding.inflate(inflater, parent, false)
                FooterViewHolder(binding, onRetry)
            }
            else -> {
                val binding = ProductCategoriesListItemBinding.inflate(inflater, parent, false)
                ProductCategoriesViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductCategoriesViewHolder -> holder.bind(getItem(position))
            is FooterViewHolder -> holder.bind(footerState)
        }
    }

    fun updateFooterState(newState: FooterState) {
        if (footerState == newState) return

        val oldState = footerState
        footerState = newState

        when {
            isFooterBecomingVisible(oldState, newState) -> notifyItemInserted(currentList.size)
            isFooterBecomingHidden(oldState, newState) -> notifyItemRemoved(currentList.size)
            else -> notifyItemChanged(currentList.size)
        }
    }

    private fun isFooterBecomingVisible(oldState: FooterState, newState: FooterState): Boolean {
        return oldState == FooterState.HIDDEN && newState != FooterState.HIDDEN
    }

    private fun isFooterBecomingHidden(oldState: FooterState, newState: FooterState): Boolean {
        return oldState != FooterState.HIDDEN && newState == FooterState.HIDDEN
    }

    private class ProductCategoriesViewHolder(
        private val binding: ProductCategoriesListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: ProductCategoryEntity) {
            binding.category.text = category.name
            // TODO: Add Click listener
        }
    }

    private class FooterViewHolder(
        private val binding: HorizontalListViewFooterLoadingBinding,
        private val onRetry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(state: FooterState) {
            binding.progressBar.isVisible = state == FooterState.LOADING
            binding.retryButton.isVisible = state == FooterState.ERROR

            if (state == FooterState.ERROR) {
                binding.retryButton.setOnClickListener { onRetry() }
            }
        }
    }

    companion object {

        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_FOOTER = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProductCategoryEntity>() {
            override fun areItemsTheSame(
                oldItem: ProductCategoryEntity,
                newItem: ProductCategoryEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ProductCategoryEntity,
                newItem: ProductCategoryEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}