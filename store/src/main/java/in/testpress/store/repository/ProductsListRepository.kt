package `in`.testpress.store.repository

import `in`.testpress.database.ProductsListEntity
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.store.models.ProductsListResponse
import `in`.testpress.store.models.Result
import `in`.testpress.store.models.asDatabaseModel
import `in`.testpress.store.network.TestpressStoreApiClient
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

open class ProductsListRepository(val context: Context) {
    private val roomProductsListDao = TestpressDatabase(context).productsListDao()

    fun fetch(forceRefresh: Boolean = true): LiveData<Resource<ProductsListEntity>> {
        return object : NetworkBoundResource<ProductsListEntity, ProductsListResponse>() {
            override fun saveNetworkResponseToDB(item: ProductsListResponse) {
                saveNetworkResponseToDatabase(item.results)
            }

            override fun shouldFetch(data: ProductsListEntity?): Boolean {
                return forceRefresh
            }

            override fun loadFromDb(): LiveData<ProductsListEntity> {
                val liveData = MediatorLiveData<ProductsListEntity>()
                liveData.addSource(fetchFromDatabase()) {
                    liveData.postValue(it)
                }
                return liveData
            }

            override fun createCall(): RetrofitCall<ProductsListResponse> {
                roomProductsListDao.delete()
                return TestpressStoreApiClient(context).productsList
            }
        }.asLiveData()
    }

    private fun saveNetworkResponseToDatabase(productsList: Result?) {
        productsList?.asDatabaseModel()?.let {
            roomProductsListDao.insert(it)
        }
    }

    private fun fetchFromDatabase(): LiveData<ProductsListEntity?> {
        return roomProductsListDao.getAll()
    }
}
