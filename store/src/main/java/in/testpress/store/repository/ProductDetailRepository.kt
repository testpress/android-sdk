package `in`.testpress.store.repository

import `in`.testpress.database.ProductDetailEntity
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.store.models.ProductDetailResponse
import `in`.testpress.store.models.asDatabaseModel
import `in`.testpress.store.network.TestpressStoreApiClient
import android.content.Context
import androidx.lifecycle.LiveData

class ProductDetailRepository(val context: Context) {

    private val roomProductDetailDao = TestpressDatabase(context).productDetailDao()

    fun fetch(forceRefresh: Boolean = false, productSlug: String): LiveData<Resource<ProductDetailEntity?>> {
        return object : NetworkBoundResource<ProductDetailEntity?, ProductDetailResponse>() {
            override fun saveNetworkResponseToDB(item: ProductDetailResponse) {
                saveNetworkResponseToDatabase(item)
            }

            override fun shouldFetch(data: ProductDetailEntity?): Boolean {
                return forceRefresh || data == null
            }

            override fun loadFromDb(): LiveData<ProductDetailEntity?> {
                return fetchFromDatabase()
            }

            override fun createCall(): RetrofitCall<ProductDetailResponse> {
                return TestpressStoreApiClient(context).getProductDetails(productSlug)
            }
        }.asLiveData()
    }

    private fun saveNetworkResponseToDatabase(productDetail: ProductDetailResponse?) {
        productDetail?.asDatabaseModel()?.let {
            roomProductDetailDao.insert(it)
        }
    }

    private fun fetchFromDatabase(): LiveData<ProductDetailEntity?> {
        return roomProductDetailDao.getAll()
    }
}
