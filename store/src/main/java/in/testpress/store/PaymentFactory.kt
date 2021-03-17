package `in`.testpress.store

import `in`.testpress.store.models.Order
import `in`.testpress.store.payu.PayuPayment
import android.app.Activity

class PaymentFactory {
    fun create(order: Order, activity: Activity): PaymentGateway {
        return PayuPayment(order, activity)
    }
}