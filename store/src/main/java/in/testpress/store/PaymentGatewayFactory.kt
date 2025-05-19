package `in`.testpress.store

import `in`.testpress.store.models.Order
import `in`.testpress.store.payu.PayuPaymentGateway
import android.app.Activity
import `in`.testpress.store.razorpay.RazorpayPaymentGateway
import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.InstituteSettings
import `in`.testpress.store.stripe.StripePaymentGateway


class PaymentGatewayFactory {
    fun create(order: Order, activity: Activity): PaymentGateway {
        val instituteSettings: InstituteSettings = TestpressSdk.getTestpressSession(activity)!!.instituteSettings
        return when (instituteSettings.currentPaymentApp?.lowercase()) {
            "razorpay" -> {
                RazorpayPaymentGateway(order, activity)
            }
            "stripe" -> {
                StripePaymentGateway(order, activity)
            }
            else -> {
                PayuPaymentGateway(order, activity)
            }
        }
    }
}
