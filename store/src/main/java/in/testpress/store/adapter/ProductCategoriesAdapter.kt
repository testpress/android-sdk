package `in`.testpress.store.adapter

import `in`.testpress.store.R
import `in`.testpress.store.models.ProductCategories
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ProductCategoriesAdapter(val context: Context): ListAdapter<ProductCategories, ProductCategoriesAdapter.ProductCategoriesListItemViewHolder>(PRODUCT_CATEGORIES_COMPARATOR) {
    var productCategories: List<ProductCategories> = listOf()

    var selectedPosition = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductCategoriesListItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_categories_list_item, parent, false)
        return ProductCategoriesListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductCategoriesListItemViewHolder, position: Int) {
        val productCategories = getItem(0)
        //if (productCategories != null) {
            holder.bind(productCategories,position)
        //}
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun getItem(position: Int): ProductCategories? {
        //if (10 > position) return productCategories[position]
        return null
    }

    inner class ProductCategoriesListItemViewHolder(view: View): RecyclerView.ViewHolder(view){
        val name  = view.findViewById<Button>(R.id.name)

        fun bind(productCategories:ProductCategories?,number: Int){
            if (number == 0){
                name.text = "All Product"
                name.background = context.getDrawable(R.drawable.testpress_blue_solid)
            } else {
                name.text = "ProductCategories"
                name.background = context.getDrawable(R.drawable.testpress_black_stroke)
            }
        }

    }

    companion object {
        private val PRODUCT_CATEGORIES_COMPARATOR = object : DiffUtil.ItemCallback<ProductCategories>() {
            override fun areContentsTheSame(oldItem: ProductCategories, newItem: ProductCategories): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: ProductCategories, newItem: ProductCategories): Boolean =
                oldItem.id == newItem.id
        }
    }
}