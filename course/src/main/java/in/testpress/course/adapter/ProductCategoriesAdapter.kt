package `in`.testpress.course.adapter

import `in`.testpress.database.entities.ProductCategoryEntity
import `in`.testpress.course.R
import `in`.testpress.course.databinding.ProductCategoriesListItemBinding
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class ProductCategoriesAdapter(val context: Context, private val categoriesListener: CategoriesListener) :
    ListAdapter<ProductCategoryEntity, ProductCategoriesAdapter.ProductCategoriesListItemViewHolder>(
        PRODUCT_CATEGORIES_COMPARATOR
    ) {
    var productCategories: MutableList<ProductCategoryEntity> = mutableListOf()
    var selectedChip = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductCategoriesListItemViewHolder {
        val binding = ProductCategoriesListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductCategoriesListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductCategoriesListItemViewHolder, position: Int) {
        val productCategories = getItem(position)
        holder.categoryChip.tag = position
        holder.categoryChip.isChecked = selectedChip == position
        holder.categoryChip.text = productCategories?.name
        if (productCategories != null) {
            holder.bind(productCategories,position){
                categoriesListener.invoke(productCategories)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return productCategories.size
    }

    override fun getItem(position: Int): ProductCategoryEntity? {
        if (productCategories.size > position) return productCategories[position]
        return null
    }

    inner class ProductCategoriesListItemViewHolder(binding: ProductCategoriesListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val categoryChip: Chip = binding.categoryChip

        fun bind(productCategories: ProductCategoryEntity,position: Int, clickListener: (ProductCategoryEntity) -> Unit) {
            categoryChip.setOnClickListener {
                clickListener(productCategories)
                selectedChip = position
            }
        }
    }

    companion object {
        private val PRODUCT_CATEGORIES_COMPARATOR =
            object : DiffUtil.ItemCallback<ProductCategoryEntity>() {
                override fun areContentsTheSame(
                    oldItem: ProductCategoryEntity,
                    newItem: ProductCategoryEntity
                ): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: ProductCategoryEntity,
                    newItem: ProductCategoryEntity
                ): Boolean =
                    oldItem.id == newItem.id
            }
    }
}

interface CategoriesListener{
    fun invoke(productCategories: ProductCategoryEntity)
}