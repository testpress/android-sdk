package `in`.testpress.store.payu

import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.InstituteSettings
import `in`.testpress.store.PaymentGateway
import `in`.testpress.store.models.Order
import `in`.testpress.store.network.StoreApiClient
import android.app.Activity
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import com.payu.base.models.ErrorResponse
import com.payu.base.models.PayUPaymentParams
import com.payu.checkoutpro.PayUCheckoutPro
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_NAME
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_STRING
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF1
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF2
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF3
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF4
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF5
import com.payu.ui.model.listeners.PayUCheckoutProListener
import com.payu.ui.model.listeners.PayUHashGenerationListener
import java.security.MessageDigest
import java.util.*

class PayuPayment(order: Order, context: Activity): PaymentGateway(order, context) {
    val instituteSettings: InstituteSettings = TestpressSdk.getTestpressSession(context)!!.instituteSettings
    val redirectURL = instituteSettings.baseUrl + StoreApiClient.URL_PAYMENT_RESPONSE_HANDLER

    override fun showPaymentPage() {
        PayUCheckoutPro.open(context, getParameters(), object : PayUCheckoutProListener {
            override fun onPaymentSuccess(o: Any) {
                paymentGatewayListener?.onPaymentSuccess()
            }

            override fun onPaymentFailure(o: Any) {
                paymentGatewayListener?.onPaymentFailure()
            }
            override fun onPaymentCancel(b: Boolean) {
                paymentGatewayListener?.onPaymentCancel()
            }
            override fun onError(errorResponse: ErrorResponse) {
                paymentGatewayListener?.onPaymentError()
            }

            override fun generateHash(map: HashMap<String, String?>, payUHashGenerationListener: PayUHashGenerationListener) {
                val hashName = map[CP_HASH_NAME]
                val hashData = map[CP_HASH_STRING]
                if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
                    val hash: String = hashString(hashData!!, "SHA-512")
                    if (!TextUtils.isEmpty(hash)) {
                        val hashMap = hashMapOf(hash to hashName)
                        payUHashGenerationListener.onHashGenerated(hashMap)
                    }
                }
            }

            override fun setWebViewProperties(webView: WebView?, o: Any?) {
                Log.d("OrderConfirmActivity", "setWebViewProperties: ")
            }
        }
        )
    }

    private fun getParameters(): PayUPaymentParams {
        val builder = PayUPaymentParams.Builder()
        builder.setAmount(order.amount)
                .setIsProduction(true)
                .setProductInfo(order.productInfo)
                .setKey(order.apikey)
                .setTransactionId(order.orderId)
                .setFirstName(order.name)
                .setEmail(order.email)
                .setSurl(redirectURL)
                .setFurl(redirectURL)
                .setAdditionalParams(getAdditionalParameters())
        return builder.build()
    }

    private fun getAdditionalParameters(): HashMap<String, Any?> {
        val additionalParams = HashMap<String, Any?>()
        additionalParams[CP_UDF1] = ""
        additionalParams[CP_UDF2] = ""
        additionalParams[CP_UDF3] = ""
        additionalParams[CP_UDF4] = ""
        additionalParams[CP_UDF5] = ""
        additionalParams[CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK] = order.mobileSdkHash
        return additionalParams
    }

    private fun hashString(input: String, algorithm: String): String {
        return MessageDigest
                .getInstance(algorithm)
                .digest(input.toByteArray())
                .fold("", { str, it -> str + "%02x".format(it) })
    }
}