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

    private val ITEM_VIEW = 1
    private val FOOTER_VIEW = 2
    private var footerState = FooterState.HIDDEN

    override fun getItemViewType(position: Int): Int {
        return if (position == currentList.size) FOOTER_VIEW else ITEM_VIEW
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == FOOTER_VIEW) {
            val binding = ListViewFooterLoadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FooterViewHolder(binding, onRetry)
        } else {
            val binding = TestpressProductListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ProductViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ProductViewHolder) {
            holder.bind(getItem(position))
        } else if (holder is FooterViewHolder) {
            holder.bind(footerState)
        }
    }

    fun updateFooterState(newState: FooterState) {
        if (footerState == newState) return
        footerState = newState
        notifyItemChanged(currentList.size)
    }

    override fun getItemCount(): Int {
        return currentList.size + if (footerState == FooterState.HIDDEN) 0 else 1
    }


    inner class ProductViewHolder(private val binding: TestpressProductListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductLiteEntity) {
            binding.title.text = product.title
            binding.price.text = "₹ ${product.price}"
            binding.totalChapters.apply {
                text = "${product.chaptersCount} Chapters"
                isVisible = product.chaptersCount > 0
            }
            binding.totalContents.apply {
                text = "${product.contentsCount} Contents"
                isVisible = product.contentsCount > 0
            }
            imageLoader?.displayImage(
                product.images?.get(0)?.small,
                binding.thumbnailImage,
                ImageUtils.getPlaceholdersOption()
            )
        }
    }

    class FooterViewHolder(
        private val binding: ListViewFooterLoadingBinding,
        private val onRetry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(state: FooterState) {
            when (state) {
                FooterState.LOADING -> {
                    binding.progressBar.isVisible = true
                    binding.errorMessageContainer.isVisible = false
                    binding.retryButton.isVisible = false
                }
                FooterState.ERROR -> {
                    binding.progressBar.isVisible = false
                    binding.errorMessageContainer.isVisible = true
                    binding.retryButton.isVisible = true
                    binding.retryButton.setOnClickListener { onRetry() }
                }
                FooterState.HIDDEN -> {
                    binding.progressBar.isVisible = false
                    binding.errorMessageContainer.isVisible = false
                    binding.retryButton.isVisible = false
                }
            }
        }
    }

    companion object {
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