package `in`.testpress.store.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.StrikethroughSpan
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
import `in`.testpress.store.databinding.TestpressProductDetailsFragmentBinding
import `in`.testpress.store.models.Order
import `in`.testpress.store.ui.viewmodel.ProductViewModel
import `in`.testpress.util.ImageUtils
import `in`.testpress.util.UILImageGetter
import `in`.testpress.util.ZoomableImageString

class ProductDetailFragment : Fragment(), EmptyViewListener {
    private var _binding: TestpressProductDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressDialog: ProgressDialog
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var productViewModel: ProductViewModel
    private var productId: Int = DEFAULT_PRODUCT_ID
    private var product: DomainProduct? = null

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
        productViewModel.product.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING ->{
                    this.product = resource.data
                    showLoadingState()
                }
                Status.SUCCESS ->{
                    this.product = resource.data
                    renderProductDetails()
                }
                Status.ERROR -> showErrorState(resource.exception)
                else -> Unit
            }
        }
        productsViewModel.load(productId!!, false)

        productViewModel.orderStatus.observe(viewLifecycleOwner) { orderStatus ->
            when (orderStatus?.status) {
                Status.LOADING -> showProgressDialog("Creating your order...")
                Status.SUCCESS -> applyCoupon(orderStatus.data)
                Status.ERROR -> {
                    // TODO: Handel Error
                }
                else -> Unit
            }
        }

        productViewModel.couponStatus.observe(viewLifecycleOwner) { couponStatus ->
            when (couponStatus?.status) {
                Status.LOADING -> showProgressDialog("Applying Coupon code...")
                Status.SUCCESS -> {
                    // TODO: Update coupon UI and Product Price
                }
                Status.ERROR -> {
                    // TODO: Handel Error
                }
                else -> Unit
            }
        }
    }

    private fun showLoadingState() {
        if (product != null) return
        binding.apply {
            emptyViewContainer.isVisible = false
            emptyViewFragment.hide()
            pbLoading.isVisible = true
            mainContent.isVisible = false
            couponAndBuyButtonContainer.isVisible = false
        }
    }

    private fun renderProductDetails() {
        product?.let { domainProduct ->

            val product = domainProduct.product

            with(binding) {
                emptyViewContainer.isVisible = false
                emptyViewFragment.hide()
                pbLoading.isVisible = false
                mainContent.isVisible = true

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
                    setOnClickListener {
                        // TODO: Open OrderConfirmActivity
                    }
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
                setOnClickListener {
                    // TODO: Hide soft keyword
                    // TODO: Create Order
                }
            }

            couponAndBuyButtonContainer.isVisible = true

                renderDescription(product.descriptionHtml)
            }
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

    private fun applyCoupon(order: Order?){
        productViewModel.applyCoupon(order, binding.coupon.toString())
    }

//    private fun updateApplyCouponUI(createdOrder: Order?){
//        updateCouponAppliedText(binding.coupon.toString(), createdOrder);
//        updatePriceDisplay(createdOrder);
//        progressDialog.dismiss();
//    }
//
//    private fun updateCouponAppliedText(couponCode: String, createdOrder: Order?) {
//        binding.couponAppliedText.setTextColor(
//            ContextCompat.getColor(
//                requireContext(),
//                `in`.testpress.store.R.color.testpress_text_gray
//            )
//        )
//        binding.couponAppliedText.setCompoundDrawablesWithIntrinsicBounds(
//            `in`.testpress.store.R.drawable.baseline_check_24,
//            0,
//            0,
//            0
//        )
//        try {
//            val originalPrice: Double = product.getPrice().toDouble()
//            val discountedPrice = createdOrder.orderItems[0].price.toDouble()
//            val savings = originalPrice - discountedPrice
//            couponAppliedText.setText("$couponCode Applied! You have saved ₹$savings on this course.")
//        } catch (e: NumberFormatException) {
//            e.printStackTrace()
//            couponAppliedText.setText("$couponCode Applied! Discount has been applied successfully.")
//        }
//        couponAppliedText.setVisibility(View.VISIBLE)
//    }
//
//    private fun updatePriceDisplay(createdOrder: Order?) {
//        val priceText: TextView = findViewById(R.id.price)
//        val newPrice = createdOrder.orderItems[0].price
//        val oldPrice: String = product.getPrice()
//        val oldPriceStrikethrough = SpannableString(oldPrice)
//        oldPriceStrikethrough.setSpan(
//            StrikethroughSpan(),
//            0,
//            oldPrice.length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
//        val finalText = SpannableStringBuilder()
//        finalText.append(newPrice).append("  ").append(oldPriceStrikethrough)
//        priceText.text = finalText
//    }

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
