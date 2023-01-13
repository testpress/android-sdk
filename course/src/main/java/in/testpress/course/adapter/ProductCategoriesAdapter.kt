package `in`.testpress.course.adapter

import `in`.testpress.store.R
import `in`.testpress.database.entities.ProductCategoryEntity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ProductCategoriesAdapter(val context: Context, private val categoriesListener: CategoriesListener) :
    ListAdapter<ProductCategoryEntity, ProductCategoriesAdapter.ProductCategoriesListItemViewHolder>(
        PRODUCT_CATEGORIES_COMPARATOR
    ) {
    var productCategories: MutableList<ProductCategoryEntity> = mutableListOf()
    var selection = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductCategoriesListItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_categories_list_item, parent, false)
        return ProductCategoriesListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductCategoriesListItemViewHolder, position: Int) {
        val productCategories = getItem(position)
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

    inner class ProductCategoriesListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<Button>(R.id.name)

        fun bind(productCategories: ProductCategoryEntity,position: Int, clickListener: (ProductCategoryEntity) -> Unit) {
            name.text = productCategories.name
            name.setOnClickListener {
                clickListener(productCategories)
                selection = position
            }
            if (selection == position){
                name.background = context.getDrawable(R.drawable.testpress_blue_solid)
            } else {
                name.background = context.getDrawable(R.drawable.testpress_black_stroke)
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