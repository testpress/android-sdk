package `in`.testpress.store.ui

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import `in`.testpress.core.TestpressException
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.store.R
import `in`.testpress.store.databinding.TestpressProductDetailsFargmentBinding
import `in`.testpress.store.ui.viewmodel.ProductViewModel
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.UILImageGetter
import `in`.testpress.util.ZoomableImageString

class ProductDetailFragment : Fragment(), EmptyViewListener {
    private var _binding: TestpressProductDetailsFargmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var productsViewModel: ProductViewModel
    private var productId: Int? = null
    //private val product: DomainProduct? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.getInt(ProductDetailsActivityV2.PRODUCT_ID) != null) {
            productId = arguments!!.getInt(ProductDetailsActivityV2.PRODUCT_ID)
        }
        productsViewModel = ProductViewModel.init(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TestpressProductDetailsFargmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEmptyViewFragment()
        observeViewModel()
    }

    private fun setupEmptyViewFragment() {
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }


    private fun observeViewModel() {
        productsViewModel.loadProduct(productId = productId!!).observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> showProductLoading(resource.data)
                Status.SUCCESS -> showProductSuccess(resource.data)
                Status.ERROR -> showProductError(resource.exception)
                else -> Unit
            }
        }
    }

    private fun showProductLoading(product: DomainProduct?) {
        // TODO: Show Loading
    }

    private fun showProductSuccess(domainProduct: DomainProduct?) {
        binding.pbLoading.isVisible = false
        val imageLoader = ImageUtils.initImageLoader(requireContext())
        val options = ImageUtils.getPlaceholdersOption()
        val productImageURL = if (domainProduct?.product?.images != null && domainProduct.product.images?.isNotEmpty() == true) domainProduct.product.images!![0].original else ""
        imageLoader.displayImage(productImageURL, binding.thumbnailImage, options)
        binding.mainContent.isVisible = true
        binding.detailLayout.title.text = domainProduct?.product?.title
        binding.buyButton.text = domainProduct?.product?.buyNowText
        binding.detailLayout.totalExamsContainer.isVisible = false
        binding.detailLayout.totalNotesContainer.isVisible = false
        binding.detailLayout.examsListContainer.isVisible = false
        binding.detailLayout.notesListContainer.isVisible = false

        binding.detailLayout.price.text = domainProduct?.product?.price

        if (domainProduct?.product?.descriptionHtml?.isEmpty() == true ) {
            binding.detailLayout.descriptionContainer.isVisible = false
            binding.detailLayout.descriptionLine.isVisible = false
        } else {
            binding.detailLayout.descriptionContainer.isVisible = true
            binding.detailLayout.descriptionLine.isVisible = true
            val html = Html.fromHtml(
                domainProduct?.product?.descriptionHtml,
                UILImageGetter(binding.detailLayout.description, requireActivity()), null
            )

            binding.detailLayout.description.setText(
                ZoomableImageString.convertString(html, requireActivity(), false),
                TextView.BufferType.SPANNABLE
            )

            binding.detailLayout.description.movementMethod = LinkMovementMethod.getInstance()
            binding.detailLayout.description.isVisible = true
        }
    }

    private fun showProductError(exception: TestpressException?) {
        // TODO: Show Error
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRetryClick() {

    }

    companion object {
        fun show(activity: FragmentActivity, containerViewId: Int, productId: Int) {
            val productDetailFragment = ProductDetailFragment()
            val bundle = Bundle()
            bundle.putInt(ProductDetailsActivityV2.PRODUCT_ID, productId)
            productDetailFragment.arguments = bundle
            activity.supportFragmentManager.beginTransaction()
                .replace(containerViewId, productDetailFragment)
                .commitAllowingStateLoss()
        }
    }
}
