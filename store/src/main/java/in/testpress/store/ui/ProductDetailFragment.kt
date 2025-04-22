package `in`.testpress.store.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import `in`.testpress.core.TestpressException
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.enums.Status
import `in`.testpress.fragments.EmptyViewFragment
import `in`.testpress.fragments.EmptyViewListener
import `in`.testpress.store.TestpressStore
import `in`.testpress.store.data.model.mapping.asProduct
import `in`.testpress.store.databinding.DialogProgressBinding
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

    private var _dialogBinding: DialogProgressBinding? = null
    private val dialogBinding get() = _dialogBinding!!

    private var loadingDialog: AlertDialog? = null
    private lateinit var emptyViewFragment: EmptyViewFragment
    private lateinit var productViewModel: ProductViewModel
    private var productId: Int = DEFAULT_PRODUCT_ID
    private var domainProduct: DomainProduct? = null
    private var order: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = requireArguments().getInt(ProductDetailsActivityV2.PRODUCT_ID)
        productViewModel = ProductViewModel.init(requireActivity(), productId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TestpressProductDetailsFragmentBinding.inflate(inflater, container, false)
        _dialogBinding = DialogProgressBinding.inflate(inflater)
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
                    this.domainProduct = resource.data
                    showLoadingState()
                }
                Status.SUCCESS ->{
                    this.domainProduct = resource.data
                    renderProductDetails()
                }
                Status.ERROR -> showErrorState(resource.exception)
                else -> Unit
            }
        }

        productViewModel.order.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> showProgressDialog("Creating your order...")
                Status.SUCCESS -> {
                    this.order = resource.data
                    applyCoupon()
                }
                Status.ERROR ->{
                    this.order = null
                    handleOrderCreationFailure(resource.exception)
                }
                else -> Unit
            }
        }

        productViewModel.coupon.observe(viewLifecycleOwner) { resource ->
            when (resource?.status) {
                Status.LOADING -> showProgressDialog("Applying Coupon code...")
                Status.SUCCESS -> {
                    this.order = resource.data
                    updateApplyCouponUI()
                }
                Status.ERROR ->{
                    this.order = null
                    handleCouponApplicationFailure(resource.exception)
                }
                else -> Unit
            }
        }
    }

    private fun showLoadingState() {
        if (domainProduct != null) return
        binding.apply {
            emptyViewContainer.isVisible = false
            emptyViewFragment.hide()
            pbLoading.isVisible = true
            mainContent.isVisible = false
            couponAndBuyButtonContainer.isVisible = false
        }
    }

    private fun renderProductDetails() {
        domainProduct?.let { domainProduct ->

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
                    setOnClickListener {
                        if (product.paymentLink.isNullOrEmpty()) {
                            val intent = Intent(requireContext(), OrderConfirmActivity::class.java)
                            intent.putExtra(ProductDetailsActivity.PRODUCT, domainProduct.asProduct())
                            intent.putExtra(ProductDetailsActivity.ORDER, order)
                            startActivityForResult(intent, TestpressStore.STORE_REQUEST_CODE)
                        } else {
                            val uri = Uri.parse(product.paymentLink)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        }
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

                couponInputEditText.addTextChangedListener(afterTextChanged = {
                    applyCouponButton.isEnabled = it?.trim()?.isNotEmpty() == true
                })

                applyCouponButton.setOnClickListener {
                    val enteredCoupon = couponInputEditText.text.toString()
                    hideKeyboard()
                    if (order == null) {
                        productViewModel.createOrder(domainProduct)
                    } else {
                        productViewModel.applyCoupon(order!!.id.toLong(), enteredCoupon)
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
        if (loadingDialog == null) {
            loadingDialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.root)
                .setCancelable(false)
                .create()
        }
        dialogBinding.messageText.text = message
        loadingDialog?.show()
    }

    private fun applyCoupon() {
        order?.let { safeOrder ->
            productViewModel.applyCoupon(
                safeOrder.id.toLong(),
                binding.couponInputEditText.text.toString()
            )
        }
    }

    private fun handleOrderCreationFailure(exception: TestpressException?) {
        loadingDialog?.dismiss()
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
        updateCouponAppliedText(binding.couponInputEditText.text.toString(), order)
        updatePriceDisplay()
        loadingDialog?.dismiss()
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
            val originalPrice: Double? = domainProduct?.product?.price?.toDouble()
            val discountedPrice = createdOrder?.orderItems?.firstOrNull()?.price?.toDouble()
            val savings = (originalPrice ?: 0.0) - (discountedPrice ?: 0.0)
            binding.couponAppliedText.text = "$couponCode Applied! You have saved ₹${"%.2f".format(savings)} on this course."
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            binding.couponAppliedText.text = "$couponCode Applied! Discount has been applied successfully."
        }
        binding.couponAppliedText.visibility = View.VISIBLE
    }

    private fun updatePriceDisplay() {
        val newPrice = order?.orderItems?.firstOrNull()?.price ?: return
        val oldPrice: String = domainProduct?.product?.price ?: ""
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
        binding.detailLayout.price.text = domainProduct?.product?.price
        if (exception?.isNetworkError == true) {
            showToast("Please check your internet connection")
        } else {
            showInvalidCouponMessage()
        }
        loadingDialog?.dismiss()
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

    private fun hideKeyboard() {
        val imm: InputMethodManager? = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        val currentFocus = requireActivity().currentFocus
        if (currentFocus != null) {
            imm?.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    override fun onRetryClick() {
        productViewModel.retry()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        loadingDialog = null
        _dialogBinding = null
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
