package `in`.testpress.store.ui.adatper

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import `in`.testpress.database.entities.ProductLiteEntity
import `in`.testpress.store.databinding.TestpressProductListItemBinding
import `in`.testpress.util.ImageUtils

class ProductListAdapter(context: Context) : ListAdapter<ProductLiteEntity, ProductListAdapter.ProductViewHolder>(ProductDiffCallback()) {

    private val imageLoader: ImageLoader? = ImageUtils.initImageLoader(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = TestpressProductListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(private val binding: TestpressProductListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductLiteEntity) {
            binding.title.text = product.title
            binding.price.text = "₹ ${product.price}"
            binding.totalChapters.text = "${product.chaptersCount} Chapters"
            binding.totalContents.text = "${product.chaptersCount} Contents"
            imageLoader?.displayImage(product.images?.get(0)?.small, binding.thumbnailImage, ImageUtils.getPlaceholdersOption())
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<ProductLiteEntity>() {
    override fun areItemsTheSame(oldItem: ProductLiteEntity, newItem: ProductLiteEntity): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: ProductLiteEntity, newItem: ProductLiteEntity): Boolean = oldItem.id == newItem.id
}