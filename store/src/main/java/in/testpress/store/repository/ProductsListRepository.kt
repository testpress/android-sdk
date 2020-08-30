package `in`.testpress.store.repository

import `in`.testpress.database.ProductsListEntity
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.store.models.ProductsList
import `in`.testpress.store.models.asDatabaseModel
import `in`.testpress.store.network.TestpressStoreApiClient
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class ProductsListRepository(val context: Context) {
    val roomProductsListDao = TestpressDatabase(context).productsListDao()

    fun fetch(forceRefresh: Boolean = false): LiveData<Resource<ProductsListEntity>> {
        return object : NetworkBoundResource<ProductsListEntity, ProductsList>() {
            override fun saveNetworkResponseToDB(item: ProductsList) {
                roomProductsListDao.insert(item.asDatabaseModel())
                saveNetworkResponseToDatabase(item)
            }

            override fun shouldFetch(data: ProductsListEntity?): Boolean {
                return forceRefresh || fetchFromDatabase() == null
            }

            override fun loadFromDb(): LiveData<ProductsListEntity> {
                val liveData = MutableLiveData<ProductsListEntity>()
                liveData.postValue(fetchFromDatabase())
                return liveData
            }

            override fun createCall(): RetrofitCall<ProductsList> {
                return TestpressStoreApiClient(context).productsList
            }
        }.asLiveData()
    }

    private fun saveNetworkResponseToDatabase(productsList: ProductsList) {
       roomProductsListDao.insert(productsList.asDatabaseModel())
    }

    private fun fetchFromDatabase(): ProductsListEntity? {
        return roomProductsListDao.getAll().value
    }
}