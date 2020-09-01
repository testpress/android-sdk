package `in`.testpress.store.ui

import `in`.testpress.store.R
import `in`.testpress.ui.BaseToolBarActivity
import android.os.Bundle

class ProductDetailActivity: BaseToolBarActivity() {

    companion object {
        const val PRODUCT_SLUG = "productSlug"
        const val PRODUCT = "product"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testpress_product_details_layout)
    }
}