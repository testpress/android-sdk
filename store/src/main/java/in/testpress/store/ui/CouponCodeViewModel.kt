package `in`.testpress.store.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.store.models.CouponCodeResponse
import `in`.testpress.store.network.Resource
import `in`.testpress.store.network.TestpressStoreApiClient
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CouponCodeViewModel(val context: Context?) : ViewModel() {

    var verifyCouponResponse: MutableLiveData<Resource<CouponCodeResponse>> = MutableLiveData()

    fun verify(orderId: Int, couponCode: String) {
        TestpressStoreApiClient(context).applyCouponCode(orderId, couponCode)
                .enqueue(object : TestpressCallback<CouponCodeResponse>() {
                    override fun onSuccess(couponCodeResponse: CouponCodeResponse) {
                        verifyCouponResponse.postValue(Resource.success(couponCodeResponse))
                    }

                    override fun onException(exception: TestpressException?) {
                        verifyCouponResponse.postValue(Resource.error(exception!!, null))
                    }
                })
    }
}