package `in`.testpress.store.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.store.R
import `in`.testpress.store.models.CouponCodeResponse
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.coupon_code_fragment.*

class CouponCodeFragment : Fragment() {

    private lateinit var couponCodeViewModel: CouponCodeViewModel
    private var orderId: Int = 0

    companion object {
        private const val ORDER_ID = "order_id"

        fun newInstance(orderId: Int): CouponCodeFragment {
            val couponCodeFragment = CouponCodeFragment()
            val bundle = Bundle()
            bundle.putInt(ORDER_ID, orderId)
            couponCodeFragment.arguments = bundle
            return couponCodeFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDataFromBundle()
        return inflater.inflate(R.layout.coupon_code_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewModel()
        setOnClickListeners()
        initializeObservers()
    }

    private fun getDataFromBundle() {
        orderId = arguments?.getInt(ORDER_ID)?.let { it }?: run { 0 }
    }

    private fun initializeViewModel() {
        couponCodeViewModel = ViewModelProvider(requireActivity()).get(CouponCodeViewModel::class.java)
    }

    private fun setOnClickListeners() {
        applycouponButton.setOnClickListener {
            hideApplyCouponButton()
            showCouponCodeContainer()
            resetCouponInput()
        }
        closeButton.setOnClickListener {
            showApplyCouponButton()
            hideAppliedCoupon()
        }
        applyButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            verifyCouponCode()
        }
    }

    private fun hideApplyCouponButton() {
        applyCouponButtonContainer.visibility = View.GONE
    }

    private fun showCouponCodeContainer() {
        couponCodeContainer.visibility = View.VISIBLE
    }

    private fun resetCouponInput() {
        couponCode.text = null
        couponCodeTextLayout.isErrorEnabled = false
    }

    private fun showApplyCouponButton() {
        applyCouponButtonContainer.visibility = View.VISIBLE
    }

    private fun hideAppliedCoupon() {
        appliedCouponContainer.visibility = View.GONE
    }

    private fun verifyCouponCode() {
        couponCodeViewModel.verify(context, orderId, couponCode.text.toString())
    }

    private fun initializeObservers() {
        couponCodeViewModel.couponCodeResult.observe(this, Observer {
            setCouponAppliedLayout(it)
        })

        couponCodeViewModel.couponCodeException.observe(this, Observer {
            progressBar.visibility = View.GONE
            handleNetworkException(it)
        })
    }

    private fun setCouponAppliedLayout(couponCodeResponse: CouponCodeResponse) {
        progressBar.visibility = View.GONE
        couponCodeContainer.visibility = View.GONE
        appliedCouponContainer.visibility = View.VISIBLE
        couponCodeResponse.voucher?.code?.let {
            couponAppliedText.text = it
        }
    }

    private fun handleNetworkException(exception: TestpressException) {
        progressBar.visibility = View.GONE
        when {
            exception.isClientError -> {
                couponCodeTextLayout.error = getString(R.string.invalid_coupon_code)
            }
        }
    }
}