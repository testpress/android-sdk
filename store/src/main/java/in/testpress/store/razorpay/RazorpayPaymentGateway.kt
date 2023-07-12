package `in`.testpress.store.razorpay

import com.razorpay.PaymentResultWithDataListener
import com.razorpay.ExternalWalletListener
import android.app.Activity
import com.razorpay.*
import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.InstituteSettings
import `in`.testpress.store.PaymentGateway
import `in`.testpress.store.models.Order
import org.json.JSONObject
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.Toast
import `in`.testpress.store.network.StoreApiClient
import java.lang.Exception


class RazorpayPaymentGateway(order: Order, context: Activity): PaymentGateway(order, context), PaymentResultWithDataListener, ExternalWalletListener, DialogInterface.OnClickListener {
    private lateinit var alertDialogBuilder: AlertDialog.Builder

    val instituteSettings: InstituteSettings = TestpressSdk.getTestpressSession(context)!!.instituteSettings
    val redirectURL = instituteSettings.baseUrl + StoreApiClient.RAZORPAY_PAYMENT_RESPONSE_PATH

    override fun showPaymentPage() {
        startPayment()
    }

    private fun startPayment() {
        val co = Checkout()
        co.setKeyID(order.apikey)
        try {
            var options = getParameters()
            co.open(context, options)
        }catch (e: Exception){
            Toast.makeText(context,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun getParameters(): JSONObject {
        val payloadHelper = PayloadHelper("INR", 1000, order.orderId)
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

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        paymentGatewayListener?.onPaymentSuccess()
        try{
            alertDialogBuilder.setMessage("Payment Successful : Payment ID: $p0\nPayment Data: ${p1?.data}")
            alertDialogBuilder.show()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        paymentGatewayListener?.onPaymentError(p1)
        try {
            alertDialogBuilder.setMessage("Payment Failed : Payment Data: ${p2?.data}")
            alertDialogBuilder.show()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onExternalWalletSelected(p0: String?, p1: PaymentData?) {
        try{
            alertDialogBuilder.setMessage("External wallet was selected : Payment Data: ${p1?.data}")
            alertDialogBuilder.show()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
    }
}
