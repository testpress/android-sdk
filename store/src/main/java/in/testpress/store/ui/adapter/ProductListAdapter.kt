package `in`.testpress.store.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import `in`.testpress.database.entities.ProductLiteEntity
import `in`.testpress.store.databinding.ListViewFooterLoadingBinding
import `in`.testpress.store.databinding.TestpressProductListItemBinding
import `in`.testpress.util.ImageUtils

class ProductListAdapter(
    context: Context,
    private val onRetry: () -> Unit
) : ListAdapter<ProductLiteEntity, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    private val imageLoader: ImageLoader? = ImageUtils.initImageLoader(context)
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
                val binding = ListViewFooterLoadingBinding.inflate(inflater, parent, false)
                FooterViewHolder(binding, onRetry)
            }
            else -> {
                val binding = TestpressProductListItemBinding.inflate(inflater, parent, false)
                ProductViewHolder(binding, imageLoader)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProductViewHolder -> holder.bind(getItem(position))
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

    private class ProductViewHolder(
        private val binding: TestpressProductListItemBinding,
        private val imageLoader: ImageLoader?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductLiteEntity) {
            binding.title.text = product.title
            binding.price.text = "₹ ${product.price}"

            binding.totalChapters.apply {
                isVisible = product.chaptersCount > 0
                text = "${product.chaptersCount} Chapters"
            }

            binding.totalContents.apply {
                isVisible = product.contentsCount > 0
                text = "${product.contentsCount} Contents"
            }

            imageLoader?.displayImage(
                product.images?.firstOrNull()?.small,
                binding.thumbnailImage,
                ImageUtils.getPlaceholdersOption()
            )
        }
    }

    private class FooterViewHolder(
        private val binding: ListViewFooterLoadingBinding,
        private val onRetry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(state: FooterState) {
            binding.progressBar.isVisible = state == FooterState.LOADING
            binding.errorMessageContainer.isVisible = state == FooterState.ERROR
            binding.retryButton.isVisible = state == FooterState.ERROR

            if (state == FooterState.ERROR) {
                binding.retryButton.setOnClickListener { onRetry() }
            }
        }
    }

    companion object {

        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_FOOTER = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProductLiteEntity>() {
            override fun areItemsTheSame(
                oldItem: ProductLiteEntity,
                newItem: ProductLiteEntity
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ProductLiteEntity,
                newItem: ProductLiteEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}

enum class FooterState {
    LOADING,
    ERROR,
    HIDDEN
}