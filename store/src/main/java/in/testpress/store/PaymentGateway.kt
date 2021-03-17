package `in`.testpress.store

import `in`.testpress.store.models.Order
import android.app.Activity

abstract class PaymentGateway(val order: Order, val context: Activity) {
    var paymentGatewayListener: PaymentGatewayListener? = null

    abstract fun showPaymentPage()
}

interface PaymentGatewayListener {
    fun onPaymentSuccess()
    fun onPaymentFailure()
    fun onPaymentError()
    fun onPaymentCancel()
}