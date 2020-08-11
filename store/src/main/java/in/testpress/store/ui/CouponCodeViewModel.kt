package `in`.testpress.store.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.store.models.CouponCodeResponse
import `in`.testpress.store.network.TestpressStoreApiClient
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CouponCodeViewModel: ViewModel() {

    var couponCodeException: MutableLiveData<TestpressException> = MutableLiveData()
    var couponCodeResult: MutableLiveData<CouponCodeResponse> = MutableLiveData()

    fun verify(context: Context?, orderId: Int, couponCode: String) {
        TestpressStoreApiClient(context).applyCouponCode(orderId, couponCode)
                .enqueue(object : TestpressCallback<CouponCodeResponse>() {
                    override fun onSuccess(couponCodeResponse: CouponCodeResponse) {
                        couponCodeResult.postValue(couponCodeResponse)
                    }

                    override fun onException(exception: TestpressException?) {
                        couponCodeException.postValue(exception)
                    }
                })
    }
}