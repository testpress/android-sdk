package `in`.testpress.store.ui

import android.os.Bundle
import `in`.testpress.store.databinding.TestpressProductDetailsLayoutBinding
import `in`.testpress.ui.BaseToolBarActivity

class ProductDetailsActivityV2 : BaseToolBarActivity() {

    private lateinit var binding: TestpressProductDetailsLayoutBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TestpressProductDetailsLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

}