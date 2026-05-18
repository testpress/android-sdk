package `in`.testpress.course.util

import `in`.testpress.course.R
import `in`.testpress.course.util.ProductUtils.getPriceForProduct
import `in`.testpress.store.ui.ProductDetailsActivity
import `in`.testpress.store.TestpressStore
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra(ProductDetailsActivity.PRODUCT_SLUG, productSlug)
            val activity = context.getActivity()
            if (activity != null) {
                activity.startActivityForResult(intent, TestpressStore.STORE_REQUEST_CODE)
            } else {
                context.startActivity(intent)
            }
        }
    }

    private fun Context.getActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }
}