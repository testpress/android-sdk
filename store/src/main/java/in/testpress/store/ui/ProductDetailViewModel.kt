package `in`.testpress.store.ui

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.store.models.ProductDetailResponse
import `in`.testpress.store.network.Resource
import `in`.testpress.store.network.TestpressStoreApiClient
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProductDetailViewModel(val context: Context) : ViewModel() {

    var productDetailResponse: MutableLiveData<Resource<ProductDetailResponse>> = MutableLiveData()

    fun get(productSlug: String): LiveData<Resource<ProductDetailResponse>> {
        TestpressStoreApiClient(context).getProductDetails(productSlug)
                .enqueue(object : TestpressCallback<ProductDetailResponse?>() {
                    override fun onSuccess(result: ProductDetailResponse?) {
                        productDetailResponse.postValue(Resource.success(result))
                    }

                    override fun onException(exception: TestpressException) {
                        productDetailResponse.postValue(Resource.error(exception, null))
                    }

                })
        return productDetailResponse
    }
}