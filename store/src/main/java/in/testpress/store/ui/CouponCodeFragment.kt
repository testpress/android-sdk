package `in`.testpress.store.ui

import `in`.testpress.store.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.coupon_code_fragment.*

class CouponCodeFragment : Fragment() {

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
        setOnClickListeners()
    }

    private fun getDataFromBundle() {
        orderId = arguments?.getInt(ORDER_ID, 0) ?: 0
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
}