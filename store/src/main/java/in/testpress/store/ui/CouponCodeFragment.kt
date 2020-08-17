package `in`.testpress.store.ui

import `in`.testpress.core.TestpressException
import `in`.testpress.store.R
import `in`.testpress.store.models.CouponCodeResponse
import `in`.testpress.store.network.Status
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.coupon_code_fragment.*

class CouponCodeFragment : Fragment() {

    private lateinit var couponCodeViewModel: CouponCodeViewModel
    private var orderId: Int = 0

    companion object {
        private const val ORDER_ID = "order_id"

        fun newInstance(orderId: Int): CouponCodeFragment {
            val couponCodeFragment = CouponCodeFragment()
            couponCodeFragment.arguments = Bundle().apply {
                putInt(ORDER_ID, orderId)
            }
            return couponCodeFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.coupon_code_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromBundle()
        initializeViewModel()
        setOnClickListeners()
        initializeObservers()
    }

    private fun getDataFromBundle() {
        orderId = arguments?.getInt(ORDER_ID, 0) ?: 0
    }

    private fun initializeViewModel() {
        couponCodeViewModel = ViewModelProvider(requireActivity(), object: ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return CouponCodeViewModel(context) as T
            }

        }).get(CouponCodeViewModel::class.java)
    }

    private fun setOnClickListeners() {
        haveCouponButton.setOnClickListener {
            hideHaveCouponButton()
            showCouponInputContainer()
        }
        changeButton.setOnClickListener {
            hideAppliedCoupon()
            showCouponInputContainer()
            resetCouponInput()
        }
        applyButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            verifyCouponCode()
        }
    }

    private fun hideHaveCouponButton() {
        haveCouponButtonContainer.visibility = View.GONE
    }

    private fun showCouponInputContainer() {
        couponInputContainer.visibility = View.VISIBLE
    }

    private fun resetCouponInput() {
        couponCode.text = null
        couponCodeTextLayout.isErrorEnabled = false
    }

    private fun hideAppliedCoupon() {
        appliedCouponContainer.visibility = View.GONE
    }

    private fun verifyCouponCode() {
        couponCodeViewModel.verify(orderId, couponCode.text.toString())
    }

    private fun initializeObservers() {
        couponCodeViewModel.verifyCouponResponse.observe(this, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> setCouponAppliedLayout(resource.data)
                Status.ERROR -> handleNetworkException(resource.exception)
            }
        })
    }

    private fun setCouponAppliedLayout(couponCodeResponse: CouponCodeResponse?) {
        progressBar.visibility = View.GONE
        couponInputContainer.visibility = View.GONE
        appliedCouponContainer.visibility = View.VISIBLE
        couponCodeResponse?.voucher?.code?.let {
            couponAppliedText.text = it
        }
    }

    private fun handleNetworkException(exception: TestpressException?) {
        progressBar.visibility = View.GONE
        if (exception?.isClientError == true) {
            couponCodeTextLayout.error = getString(R.string.invalid_coupon_code)
        }
    }
}