package `in`.testpress.course.adapter

import `in`.testpress.store.R
import `in`.testpress.course.models.ProductCategories
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ProductCategoriesAdapter(val context: Context) :
    ListAdapter<ProductCategories, ProductCategoriesAdapter.ProductCategoriesListItemViewHolder>(
        PRODUCT_CATEGORIES_COMPARATOR
    ) {
    var productCategories: MutableList<ProductCategories> = mutableListOf()

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
            holder.bind(productCategories){
                t(it,holder.itemView.context)
            }
        }
    }

    fun t(productCategories: ProductCategories, context: Context){
        Toast.makeText(context,productCategories.name.toString(),Toast.LENGTH_SHORT).show()
    }

    fun addProductCategories(list: List<ProductCategories>) {
        val defaultProductCategories = ProductCategories(id = 0, name = "All Product", null)
        productCategories.add(defaultProductCategories)
        productCategories.addAll(list)
    }

    override fun getItemCount(): Int {
        return productCategories.size
    }

    override fun getItem(position: Int): ProductCategories? {
        if (productCategories.size > position) return productCategories[position]
        return null
    }

    inner class ProductCategoriesListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<Button>(R.id.name)

        fun bind(productCategories: ProductCategories, clickListener: (ProductCategories) -> Unit) {
            name.text = productCategories.name
            //name.background = context.getDrawable(R.drawable.testpress_blue_solid)
            name.background = context.getDrawable(R.drawable.testpress_black_stroke)
            itemView.setOnClickListener { clickListener(productCategories) }
        }

    }

    companion object {
        private val PRODUCT_CATEGORIES_COMPARATOR =
            object : DiffUtil.ItemCallback<ProductCategories>() {
                override fun areContentsTheSame(
                    oldItem: ProductCategories,
                    newItem: ProductCategories
                ): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(
                    oldItem: ProductCategories,
                    newItem: ProductCategories
                ): Boolean =
                    oldItem.id == newItem.id
            }
    }
}