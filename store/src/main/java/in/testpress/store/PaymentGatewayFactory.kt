package `in`.testpress.store

import `in`.testpress.store.models.Order
import `in`.testpress.store.payu.PayuPaymentGateway
import android.app.Activity
import `in`.testpress.store.razorpay.RazorpayPaymentGateway
import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.InstituteSettings
import `in`.testpress.store.network.StoreApiClient


class PaymentGatewayFactory {
    fun create(order: Order, activity: Activity): PaymentGateway {
        val instituteSettings: InstituteSettings = TestpressSdk.getTestpressSession(activity)!!.instituteSettings
        val redirectURL = instituteSettings.baseUrl + StoreApiClient.RAZORPAY_PAYMENT_RESPONSE_PATH

        if (instituteSettings.currentPaymentApp.lowercase() == "razorpay")
            return RazorpayPaymentGateway(order, activity)
        return PayuPaymentGateway(order, activity)
    }
}