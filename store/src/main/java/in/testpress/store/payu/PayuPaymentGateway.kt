package `in`.testpress.store.payu

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSdk
import `in`.testpress.models.InstituteSettings
import `in`.testpress.store.PaymentGateway
import `in`.testpress.store.models.NetworkHash
import `in`.testpress.store.models.Order
import `in`.testpress.store.network.StoreApiClient
import android.app.Activity
import android.text.TextUtils
import android.webkit.WebView
import com.payu.base.models.*
import com.payu.checkoutpro.PayUCheckoutPro
import com.payu.checkoutpro.models.PayUCheckoutProConfig
import com.payu.checkoutpro.utils.PayUCheckoutProConstants
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
import java.util.*
import kotlin.collections.HashMap


class PayuPaymentGateway(order: Order, context: Activity): PaymentGateway(order, context), PayUCheckoutProListener {
    val instituteSettings: InstituteSettings = TestpressSdk.getTestpressSession(context)!!.instituteSettings
    val redirectURL = instituteSettings.baseUrl + StoreApiClient.URL_PAYMENT_RESPONSE_HANDLER
    val storeApiClient = StoreApiClient(context)
    val payuConfig = PayUCheckoutProConfig()

    init {
        addAdditionalPaymentModes()
        addOrderDetailsToCart()
    }

    override fun showPaymentPage() {
        PayUCheckoutPro.open(context, getParameters(), payuConfig, this)
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

    private fun addAdditionalPaymentModes() {
        val checkoutOrderList = ArrayList<PaymentMode>()
        checkoutOrderList.add(PaymentMode(PaymentType.UPI, PayUCheckoutProConstants.CP_GOOGLE_PAY))
        checkoutOrderList.add(PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PHONEPE))
        checkoutOrderList.add(PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PAYTM))
        payuConfig.paymentModesOrder = checkoutOrderList
    }

    private fun addOrderDetailsToCart() {
        val orderDetailsList = ArrayList<OrderDetails>()
        orderDetailsList.add(OrderDetails(order.productInfo, "1"))
        payuConfig.cartDetails = orderDetailsList
    }

    override fun generateHash(map: HashMap<String, String?>, hashGenerationListener: PayUHashGenerationListener) {
        val hashName = map[CP_HASH_NAME]
        val hashData = map[CP_HASH_STRING]

        if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
            storeApiClient.generateHash(hashData).enqueue(object : TestpressCallback<NetworkHash>(){
                override fun onSuccess(result: NetworkHash?) {
                    val hash = result?.hash ?: ""
                    val dataMap: HashMap<String, String?> = HashMap()
                    dataMap[hashName!!] = hash
                    hashGenerationListener.onHashGenerated(dataMap)
                }

                override fun onException(exception: TestpressException?) {
                    val dataMap: HashMap<String, String?> = HashMap()
                    dataMap[hashName!!] = ""
                    hashGenerationListener.onHashGenerated(dataMap)
                }
            })
        }
    }

    override fun onError(errorResponse: ErrorResponse) {
        paymentGatewayListener?.onPaymentError(errorResponse.errorMessage)
    }

    override fun onPaymentCancel(isTxnInitiated: Boolean) {
        paymentGatewayListener?.onPaymentCancel()
    }

    override fun onPaymentFailure(response: Any) {
        paymentGatewayListener?.onPaymentFailure()
    }

    override fun onPaymentSuccess(response: Any) {
        paymentGatewayListener?.onPaymentSuccess()
    }

    override fun setWebViewProperties(webView: WebView?, bank: Any?) {

    }
}