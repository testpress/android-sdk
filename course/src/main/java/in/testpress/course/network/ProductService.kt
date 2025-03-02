package `in`.testpress.course.network

import `in`.testpress.core.TestpressSdk
import `in`.testpress.network.NetworkProductResponse
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.TestpressApiClient
import `in`.testpress.store.network.StoreApiClient
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import retrofit2.http.GET

interface ProductService {

    @GET(StoreApiClient.V4_PRODUCTS_LIST_PATH)
    fun getProducts(): RetrofitCall<ApiResponse<NetworkProductResponse>>
}

class ProductNetwork(context: Context) : TestpressApiClient(context, TestpressSdk.getTestpressSession(context)) {

    fun getProductService() = retrofit.create(ProductService::class.java)

    fun getProducts(): RetrofitCall<ApiResponse<NetworkProductResponse>>{
        return getProductService().getProducts()
    }

}

