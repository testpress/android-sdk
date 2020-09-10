package `in`.testpress.course.util

import `in`.testpress.course.R
import `in`.testpress.course.util.ProductUtils.getPriceForProduct
import `in`.testpress.store.ui.ProductDetailActivity
import `in`.testpress.store.ui.ProductDetailsActivity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button

object UIUtils {
    @JvmStatic
    fun displayBuyNowButton(button: Button, productSlug: String, context: Context) {
        button.visibility = View.VISIBLE

        if (getPriceForProduct(productSlug, context) > 0.0) {
            button.setText(R.string.buy_now)
        } else {
            button.setText(R.string.get_it_for_free)
        }

        button.setOnClickListener {
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra(ProductDetailActivity.PRODUCT_SLUG, productSlug)
            context.startActivity(intent)
        }
    }
}