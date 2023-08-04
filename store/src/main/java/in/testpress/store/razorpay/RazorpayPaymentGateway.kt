package `in`.testpress.store.razorpay

import android.app.Activity
import com.razorpay.*
import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.InstituteSettings
import `in`.testpress.store.PaymentGateway
import `in`.testpress.store.models.Order
import org.json.JSONObject
import `in`.testpress.store.network.StoreApiClient


class RazorpayPaymentGateway(order: Order, context: Activity): PaymentGateway(order, context) {
    val instituteSettings: InstituteSettings = TestpressSdk.getTestpressSession(context)!!.instituteSettings
    val redirectURL = instituteSettings.baseUrl + StoreApiClient.RAZORPAY_PAYMENT_RESPONSE_PATH

    override fun showPaymentPage() {
        startPayment()
    }

    private fun startPayment() {
        val co = Checkout()
        co.setKeyID(order.apikey)
        co.open(context, getParameters())
    }

    private fun getParameters(): JSONObject {
        var amount = (order.amount.toFloat() * 100).toInt() // INR in paisa
        val payloadHelper = PayloadHelper("INR", amount, order.orderId)
        payloadHelper.name = order.name
        payloadHelper.prefillEmail = order.email
        payloadHelper.prefillContact = order.phone
        payloadHelper.prefillName = instituteSettings.appName
        payloadHelper.sendSmsHash = true
        payloadHelper.retryMaxCount = 4
        payloadHelper.retryEnabled = true
        payloadHelper.color = "#000000"
        payloadHelper.allowRotation = true
        payloadHelper.rememberCustomer = true
        payloadHelper.redirect = true
        payloadHelper.callbackUrl = redirectURL
        payloadHelper.modalConfirmClose = true
        payloadHelper.backDropColor = "#ffffff"
        payloadHelper.hideTopBar = true
        payloadHelper.readOnlyEmail = true
        payloadHelper.readOnlyContact = true
        payloadHelper.readOnlyName = true
        payloadHelper.image = instituteSettings.appToolbarLogo
        payloadHelper.sendSmsHash = true
        return payloadHelper.getJson()
    }
}
