package `in`.testpress.store.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.store.data.model.NetworkProduct
import `in`.testpress.store.data.model.toPriceEntities
import `in`.testpress.store.data.model.toProductEntity
import `in`.testpress.store.network.StoreApiClient

open class ProductRepository(context: Context) {

    private val appContext = context.applicationContext
    private val database = TestpressDatabase.invoke(appContext)
    private val apiClient = StoreApiClient(appContext)

    fun loadProduct(productId: Int, forceRefresh: Boolean = false): LiveData<Resource<DomainProduct>> {
        return object : NetworkBoundResource<DomainProduct, NetworkProduct>() {

            override suspend fun saveNetworkResponseToDB(item: NetworkProduct) {
                database.productEntityDao().apply {
                    insertProduct(item.toProductEntity())
                    insertPrices(item.toPriceEntities())
                }
            }

            override fun shouldFetch(data: DomainProduct?): Boolean {
                return forceRefresh || data == null
            }

            override fun loadFromDb(): LiveData<DomainProduct> {
                return database.productEntityDao().getProduct(productId)
            }

            override fun createCall(): RetrofitCall<NetworkProduct> {
                return apiClient.getProductDetailV3(productId)
            }

        }.asLiveData()
    }
}
