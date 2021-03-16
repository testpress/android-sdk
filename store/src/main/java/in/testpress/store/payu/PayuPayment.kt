package `in`.testpress.store.payu

import `in`.testpress.store.models.Order
import android.content.Context
import com.payu.base.models.PayUPaymentParams
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF1
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF2
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF3
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF4
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_UDF5
import java.util.*

class PayuPayment(val order: Order, val context: Context) {
    fun initializeParameters(): PayUPaymentParams {
        val builder = PayUPaymentParams.Builder()
        builder.setAmount(order.amount)
                .setIsProduction(true)
                .setProductInfo(order.productInfo)
                .setKey(order.apikey)
                .setTransactionId(order.orderId)
                .setFirstName(order.name)
                .setEmail(order.email)
                .setSurl(redirectUrl)
                .setFurl(redirectUrl)
                .setAdditionalParams(getAdditionalParameters())
        return builder.build()
    }

    fun getAdditionalParameters(): HashMap<String, Any?> {
        val additionalParams = HashMap<String, Any?>()
        additionalParams[CP_UDF1] = ""
        additionalParams[CP_UDF2] = ""
        additionalParams[CP_UDF3] = ""
        additionalParams[CP_UDF4] = ""
        additionalParams[CP_UDF5] = ""
        additionalParams[CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK] = order.mobileSdkHash
        return additionalParams
    }
}