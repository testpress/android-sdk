package `in`.testpress.store.stripe

import android.app.Activity
import `in`.testpress.store.PaymentGateway
import `in`.testpress.store.TestpressStore
import `in`.testpress.store.models.Order
import `in`.testpress.store.ui.OrderConfirmActivity

class StripePaymentGateway(order: Order, context: Activity) : PaymentGateway(order, context) {

    override fun showPaymentPage() {
        if (context is OrderConfirmActivity) {
            context.showStripePaymentSheet(order)
        }
    }
}
