package `in`.testpress.store.ui

import `in`.testpress.database.CoursesItem
import `in`.testpress.database.ProductsItem
import `in`.testpress.store.R
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.testpress_products_list_item.view.*

class ProductListAdapter: RecyclerView.Adapter<ProductListAdapter.ProductsListViewHolder>() {

    private var coursesItemList: List<CoursesItem> = listOf()
    private var productsItemList: List<ProductsItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.testpress_products_list_item, parent, false)
        return ProductsListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return minOf(coursesItemList.size, productsItemList.size)
    }

    override fun onBindViewHolder(holder: ProductsListViewHolder, position: Int) {
        holder.bindItems(coursesItemList[position],productsItemList[position])
    }

    fun setData(coursesList: List<CoursesItem>?, productsList: List<ProductsItem>?) {
        productsItemList = productsList ?: listOf()
        coursesItemList = coursesList ?: listOf()
    }

    class ProductsListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindItems(coursesItem: CoursesItem, productsItem: ProductsItem) {
            itemView.title.text = coursesItem.title
            itemView.price.text = "\u20B9 ${productsItem.currentPrice}"
            itemView.totalVideos.text = "${coursesItem.videosCount} Videos"
            itemView.totalExams.text = "${coursesItem.examsCount} Exams"
            itemView.totalDocs.text = "${coursesItem.contentsCount} Docs"
            Picasso.get().load(coursesItem.image).into(itemView.thumbnailImage)
        }
    }
}
