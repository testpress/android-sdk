package `in`.testpress.store.ui

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.enums.Status
import `in`.testpress.store.data.repository.ProductDetailRepository
import `in`.testpress.store.databinding.TestpressProductDetailsDescriptionFragmentBinding
import `in`.testpress.store.databinding.TestpressProductDetailsFragmentV2Binding
import `in`.testpress.store.ui.viewmodel.ProductViewModel
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.UILImageGetter
import `in`.testpress.util.ZoomableImageString

class ProductDetailFragmentV2 : Fragment() {
    private var _binding: TestpressProductDetailsFragmentV2Binding? = null
    private val binding get() = _binding!!
    private var productId: Int = DEFAULT_PRODUCT_ID
    private var domainProduct: DomainProduct? = null

    private val tabTitles = listOf("Description", "Course Curriculum")

    private val productViewModel: ProductViewModel by lazy {
        ViewModelProvider(requireActivity(), ProductViewModelFactory(requireContext(), productId))
            .get(ProductViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = requireArguments().getInt(ProductDetailsActivityV2.PRODUCT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TestpressProductDetailsFragmentV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeProduct()
        val pagerAdapter = ProductDetailsPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun observeProduct() {
        productViewModel.product.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    this.domainProduct = resource.data
                    renderProductDetails()
                }
                Status.ERROR -> {}
                else -> Unit
            }
        }
    }

    private fun renderProductDetails() {
        val imageUrl = domainProduct?.product?.images?.get(0)?.original
        ImageUtils.initImageLoader(requireContext()).displayImage(
            imageUrl, binding.productThumbnail, ImageUtils.getPlaceholdersOption()
        )
        binding.title.text = domainProduct?.product?.title
        binding.price.text = String.format("₹%s", domainProduct?.product?.price)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val DEFAULT_PRODUCT_ID = -1
        fun show(activity: FragmentActivity, containerViewId: Int, productId: Int) {
            val fragment = ProductDetailFragmentV2().apply {
                arguments = Bundle().apply {
                    putInt(ProductDetailsActivityV2.PRODUCT_ID, productId)
                }
            }
            activity.supportFragmentManager.beginTransaction()
                .replace(containerViewId, fragment)
                .commit()
        }
    }

    private inner class ProductDetailsPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = tabTitles.size

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProductDescriptionFragment()
                1 -> ProductCurriculumFragment()
                else -> throw IllegalArgumentException("Invalid tab index")
            }
        }
    }
}

class ProductViewModelFactory(
    private val context: Context,
    private val productId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductViewModel(ProductDetailRepository(context, productId)) as T
    }
}

class ProductDescriptionFragment : Fragment() {

    private var _binding: TestpressProductDetailsDescriptionFragmentBinding? = null
    private val binding get() = _binding!!
    private var domainProduct: DomainProduct? = null

    private val productViewModel: ProductViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            TestpressProductDetailsDescriptionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeProduct()
    }

    private fun observeProduct() {
        productViewModel.product.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    this.domainProduct = resource.data
                    renderDescription()
                }
                Status.ERROR -> {}
                else -> Unit
            }
        }
    }

    private fun renderDescription() {
        domainProduct?.let { domainProduct ->
            val product = domainProduct.product
            val hasDescription = !product.descriptionHtml.isNullOrEmpty()

            if (hasDescription) {
                val html = Html.fromHtml(
                    product.descriptionHtml,
                    UILImageGetter(binding.description, requireActivity()),
                    null
                )

                binding.description.apply {
                    setText(
                        ZoomableImageString.convertString(html, requireActivity(), false),
                        TextView.BufferType.SPANNABLE
                    )
                    movementMethod = LinkMovementMethod.getInstance()
                    isVisible = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val textView = TextView(requireContext())
//        textView.text = "Product Overview"
//        textView.textSize = 18f
//        return NestedScrollView(requireContext()).apply {
//            addView(textView)
//        }
//    }
}

class ProductCurriculumFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(requireContext())
        textView.text = "Curriculum Content"
        textView.textSize = 18f
        return NestedScrollView(requireContext()).apply {
            addView(textView)
        }
    }
}

