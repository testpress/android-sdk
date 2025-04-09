package `in`.testpress.store.ui

import android.os.Bundle
import `in`.testpress.store.databinding.TestpressProductDetailsV2LayoutBinding
import `in`.testpress.ui.BaseToolBarActivity

class ProductDetailsActivityV2 : BaseToolBarActivity() {

    private lateinit var binding: TestpressProductDetailsV2LayoutBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TestpressProductDetailsV2LayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

}