package `in`.testpress.store.ui

import android.content.Intent
import android.os.Bundle
import `in`.testpress.databinding.TestpressContainerLayoutBinding
import `in`.testpress.store.TestpressStore
import `in`.testpress.ui.BaseToolBarActivity

class ProductDetailsActivityV2 : BaseToolBarActivity() {

    private lateinit var binding: TestpressContainerLayoutBinding
    private var productId: Int = 326

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TestpressContainerLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent?.extras?.getInt(PRODUCT_ID) != null) {
            productId = intent?.extras!!.getInt(PRODUCT_ID)
        }
        ProductDetailFragment.show(this, binding.fragmentContainer.id, productId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TestpressStore.STORE_REQUEST_CODE && resultCode == RESULT_OK && data!!.getBooleanExtra(
                TestpressStore.PAYMENT_SUCCESS,
                false
            )
        ) {
            setResult(RESULT_OK, data)
            finish()
        }
    }

    companion object {
        const val PRODUCT_ID = "productId"
    }
}