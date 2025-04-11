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
    private lateinit var productViewModel: ProductViewModel
    private var productId: Int = DEFAULT_PRODUCT_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = requireArguments().getInt(ProductDetailsActivityV2.PRODUCT_ID)
        productViewModel = ProductViewModel.init(requireActivity())
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
        initEmptyView()
        observeProduct()
    }

    private fun initEmptyView() {
        emptyViewFragment = EmptyViewFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.empty_view_container, emptyViewFragment)
            .commit()
    }


    private fun observeProduct() {
        productViewModel.product.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> showLoadingState()
                Status.SUCCESS -> renderProductDetails(resource.data)
                Status.ERROR -> showErrorState(resource.exception)
                else -> Unit
            }
        }
        productViewModel.loadProduct(productId, forceRefresh = false)
    }

    private fun showLoadingState() {
        binding.apply {
            emptyViewContainer.isVisible = false
            emptyViewFragment.hide()
            pbLoading.isVisible = true
            mainContent.isVisible = false
            buyButton.isVisible = false
        }
    }

    private fun renderProductDetails(domainProduct: DomainProduct?) {
        domainProduct ?: return

        with(binding) {
            emptyViewContainer.isVisible = false
            emptyViewFragment.hide()
            pbLoading.isVisible = false
            mainContent.isVisible = true

            val product = domainProduct.product
            val imageUrl = product.images?.firstOrNull()?.original.orEmpty()

            ImageUtils.initImageLoader(requireContext()).displayImage(
                imageUrl, thumbnailImage, ImageUtils.getPlaceholdersOption()
            )

            detailLayout.apply {
                title.text = product.title
                price.text = product.price
                totalExamsContainer.isVisible = false
                totalNotesContainer.isVisible = false
                examsListContainer.isVisible = false
                notesListContainer.isVisible = false
            }

            buyButton.apply {
                text = product.buyNowText
                isVisible = true
            }

            renderDescription(product.descriptionHtml)
        }
    }

    private fun renderDescription(descriptionHtml: String?) {
        val hasDescription = !descriptionHtml.isNullOrEmpty()
        binding.detailLayout.apply {
            descriptionContainer.isVisible = hasDescription
            descriptionLine.isVisible = hasDescription
        }

        if (hasDescription) {
            val html = Html.fromHtml(
                descriptionHtml,
                UILImageGetter(binding.detailLayout.description, requireActivity()),
                null
            )

            binding.detailLayout.description.apply {
                setText(
                    ZoomableImageString.convertString(html, requireActivity(), false),
                    TextView.BufferType.SPANNABLE
                )
                movementMethod = LinkMovementMethod.getInstance()
                isVisible = true
            }
        }
    }

    private fun showErrorState(exception: TestpressException?) {
        binding.pbLoading.isVisible = false
        binding.emptyViewContainer.isVisible = true
        exception?.let(emptyViewFragment::displayError)
    }

    override fun onRetryClick() {
        productViewModel.loadProduct(productId, forceRefresh = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val DEFAULT_PRODUCT_ID = -1
        fun show(activity: FragmentActivity, containerViewId: Int, productId: Int) {
            val fragment = ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ProductDetailsActivityV2.PRODUCT_ID, productId)
                }
            }
            activity.supportFragmentManager.beginTransaction()
                .replace(containerViewId, fragment)
                .commitAllowingStateLoss()
        }
    }
}
