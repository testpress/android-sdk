package `in`.testpress.store.ui

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import `in`.testpress.core.TestpressException
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.store.databinding.TestpressProductDetailsFragmentBinding
import `in`.testpress.store.models.Order
import `in`.testpress.store.ui.viewmodel.ProductViewModel
import `in`.testpress.store.util.generateRandom10CharString
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.UILImageGetter
import `in`.testpress.util.ZoomableImageString
import io.sentry.Sentry

class ProductDetailFragment : Fragment(), EmptyViewListener {
    private var _binding: TestpressProductDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressDialog: ProgressDialog
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var productViewModel: ProductViewModel
    private var productId: Int = DEFAULT_PRODUCT_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = requireArguments().getInt(ProductDetailsActivityV2.PRODUCT_ID)
        productViewModel = ProductViewModel.init(requireActivity(), productId)
        progressDialog = ProgressDialog(requireActivity());
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TestpressProductDetailsFragmentBinding.inflate(inflater, container, false)
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
            .replace(binding.emptyViewContainer.id, emptyViewFragment)
            .commit()
    }

    private fun observeProduct() {
        productViewModel.productStatus.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> showLoadingState()
                Status.SUCCESS -> renderProductDetails()
                Status.ERROR -> showErrorState(resource.exception)
                else -> Unit
            }
        }

        productViewModel.orderStatus.observe(viewLifecycleOwner) { orderStatus ->
            when (orderStatus?.status) {
                Status.LOADING -> showProgressDialog("Creating your order...")
                Status.SUCCESS -> applyCoupon()
                Status.ERROR -> handleOrderCreationFailure(orderStatus.exception)
                else -> Unit
            }
        }

        productViewModel.couponStatus.observe(viewLifecycleOwner) { couponStatus ->
            when (couponStatus?.status) {
                Status.LOADING -> showProgressDialog("Applying Coupon code...")
                Status.SUCCESS -> updateApplyCouponUI()
                Status.ERROR -> handleCouponApplicationFailure(couponStatus.exception)
                else -> Unit
            }
        }
    }

    private fun showLoadingState() {
        if (productViewModel.product != null) return
        binding.apply {
            emptyViewContainer.isVisible = false
            emptyViewFragment.hide()
            pbLoading.isVisible = true
            mainContent.isVisible = false
            couponAndBuyButtonContainer.isVisible = false
        }
    }

    private fun renderProductDetails() {
        val domainProduct = productViewModel.product
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
            }

            discountPrompt.apply {
                setOnClickListener {
                    if (discountContainer.visibility == View.GONE) {
                        discountContainer.visibility = View.VISIBLE;
                        discountPrompt.visibility = View.GONE;
                    }
                }
            }

            coupon.apply {
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        applyCoupon.isEnabled = p0.toString().trim().isNotEmpty()
                    }
                    override fun afterTextChanged(p0: Editable?) {}
                })
            }

            applyCoupon.setOnClickListener {
                productViewModel.createOrder()
            }

            couponAndBuyButtonContainer.isVisible = true

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

    private fun showProgressDialog(message: String) {
        progressDialog.setMessage(message)
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    private fun applyCoupon(){
        productViewModel.applyCoupon(binding.coupon.text.toString())
    }

    private fun handleOrderCreationFailure(exception: TestpressException?) {
        progressDialog.dismiss()
        if (exception?.isNetworkError == true) {
            showToast("Please check your internet connection")
        } else {
            val orderCreationId: String = generateRandom10CharString()
            exception?.let {
                Sentry.captureException(it) { scope ->
                    scope.setTag("orderCreationId", orderCreationId)
                }
            }
            showToast("Failed to create order. Please contact support with ID: $orderCreationId")
        }
    }

    private fun updateApplyCouponUI(){
        updateCouponAppliedText(binding.coupon.text.toString(), productViewModel.couponOrder)
        updatePriceDisplay()
        progressDialog.dismiss()
    }

    private fun updateCouponAppliedText(couponCode: String, createdOrder: Order?) {
        binding.couponAppliedText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                `in`.testpress.store.R.color.testpress_text_gray
            )
        )
        binding.couponAppliedText.setCompoundDrawablesWithIntrinsicBounds(
            `in`.testpress.store.R.drawable.baseline_check_24,
            0,
            0,
            0
        )
        try {
            val originalPrice: Double? = productViewModel.product?.product?.price?.toDouble()
            val discountedPrice = createdOrder?.orderItems?.get(0)?.price?.toDouble()
            val savings = (originalPrice ?: 0.0) - (discountedPrice ?: 0.0)
            binding.couponAppliedText.text = "$couponCode Applied! You have saved ₹$savings on this course."
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            binding.couponAppliedText.text = "$couponCode Applied! Discount has been applied successfully."
        }
        binding.couponAppliedText.visibility = View.VISIBLE
    }

    private fun updatePriceDisplay() {
        val newPrice = productViewModel.couponOrder?.orderItems?.get(0)?.price
        val oldPrice: String = productViewModel.product?.product?.price ?: ""
        val oldPriceStrikethrough = SpannableString(oldPrice)
        oldPriceStrikethrough.setSpan(
            StrikethroughSpan(),
            0,
            oldPrice.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val finalText = SpannableStringBuilder()
        finalText.append(newPrice).append("  ").append(oldPriceStrikethrough)
        binding.detailLayout.price.text = finalText
    }

    private fun handleCouponApplicationFailure(exception: TestpressException?) {
        binding.detailLayout.price.text = productViewModel.product?.product?.price
        if (exception?.isNetworkError == true) {
            showToast("Please check your internet connection")
        } else {
            showInvalidCouponMessage()
        }
        progressDialog.dismiss()
    }


    private fun showInvalidCouponMessage() {
        binding.couponAppliedText.visibility = View.VISIBLE
        binding.couponAppliedText.text = "Invalid coupon code."
        binding.couponAppliedText.setTextColor(Color.RED)
        binding.couponAppliedText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onRetryClick() {
        productViewModel.retry()
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
                .commit()
        }
    }
}
