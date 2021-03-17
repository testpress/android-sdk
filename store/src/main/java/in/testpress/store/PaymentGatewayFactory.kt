package `in`.testpress.store

import `in`.testpress.store.models.Order
import `in`.testpress.store.payu.PayuPaymentGateway
import android.app.Activity

class PaymentGatewayFactory {
    fun create(order: Order, activity: Activity): PaymentGateway {
        return PayuPaymentGateway(order, activity)
    }
}