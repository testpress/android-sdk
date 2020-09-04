package `in`.testpress.store.repository

import `in`.testpress.database.ProductsListEntity
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.store.models.*
import `in`.testpress.store.network.TestpressStoreApiClient
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

open class ProductsListRepository(val context: Context) {
    private val roomProductsListDao = TestpressDatabase(context).productsListDao()

    fun fetch(forceRefresh: Boolean = false): LiveData<Resource<ProductsListEntity>> {
        return object : NetworkBoundResource<ProductsListEntity, ProductListResponse>() {
            override fun saveNetworkResponseToDB(item: ProductListResponse) {
                saveNetworkResponseToDatabase(item.results)
            }

            override fun shouldFetch(data: ProductsListEntity?): Boolean {
                return forceRefresh || data == null
            }

            override fun loadFromDb(): LiveData<ProductsListEntity> {
                val liveData = MediatorLiveData<ProductsListEntity>()
                liveData.addSource(fetchFromDatabase()) {
                    liveData.postValue(it)
                }
                return liveData
            }

            override fun createCall(): RetrofitCall<ProductListResponse> {
                return TestpressStoreApiClient(context).productsList
            }
        }.asLiveData()
    }

    private fun saveNetworkResponseToDatabase(productsList: ProductsList?) {
        productsList?.asDatabaseModel()?.let {
            roomProductsListDao.insert(it)
        }
    }

    private fun fetchFromDatabase(): LiveData<ProductsListEntity?> {
        return roomProductsListDao.getAll()
    }
}
