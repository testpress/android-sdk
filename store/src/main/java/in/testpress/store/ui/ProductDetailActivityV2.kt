package `in`.testpress.store.ui

import android.os.Bundle
import `in`.testpress.store.databinding.TestpressProductDetailsV2LayoutBinding
import `in`.testpress.store.ui.viewmodel.ProductViewModel
import `in`.testpress.ui.BaseToolBarActivity

class ProductDetailsActivityV2 : BaseToolBarActivity()  {

    private lateinit var binding: TestpressProductDetailsV2LayoutBinding
    private lateinit var productsViewModel: ProductViewModel
    private var productId: Long = 326

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TestpressProductDetailsV2LayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        productsViewModel = ProductViewModel.init(this)
        productId = intent.getLongExtra(ProductDetailsActivity.PRODUCT_SLUG,326)
        productsViewModel.loadProduct(productId)
    }

}