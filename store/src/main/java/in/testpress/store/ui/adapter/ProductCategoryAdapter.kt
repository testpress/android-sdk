package `in`.testpress.store.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import `in`.testpress.database.entities.ProductCategoryEntity
import `in`.testpress.store.databinding.HorizontalListViewFooterLoadingBinding
import `in`.testpress.store.databinding.ProductCategoriesListItemBinding

class ProductCategoryAdapter(
    private val onRetry: () -> Unit,
    private val onCategorySelected: (ProductCategoryEntity) -> Unit
) : ListAdapter<ProductCategoryEntity, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private var footerState = FooterState.HIDDEN
    var selectedCategoryPosition = 0

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
            is ProductCategoriesViewHolder -> holder.bind(getItem(position), position)
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

    inner class ProductCategoriesViewHolder(
        private val binding: ProductCategoriesListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: ProductCategoryEntity, position: Int) {
            binding.category.text = category.name
            binding.category.isChecked = selectedCategoryPosition == position

            binding.category.setOnClickListener {
                if (selectedCategoryPosition != position) {
                    val previousSelected = selectedCategoryPosition
                    selectedCategoryPosition = position
                    notifyItemChanged(previousSelected)
                    notifyItemChanged(position)

                    onCategorySelected(category)
                }
            }
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