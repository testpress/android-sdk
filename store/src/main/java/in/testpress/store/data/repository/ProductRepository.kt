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

open class ProductRepository(
    val context: Context
) {
    protected val database = TestpressDatabase.invoke(context.applicationContext)
    private val storeApiClient = StoreApiClient(context)

    fun loadProduct(
        productId: Int,
        forceRefresh: Boolean = false
    ): LiveData<Resource<DomainProduct>> {
        return object : NetworkBoundResource<DomainProduct, NetworkProduct>() {
            override suspend fun saveNetworkResponseToDB(item: NetworkProduct) {
                database.productEntityDao().insertProduct(item.toProductEntity())
                database.productEntityDao().insertPrices(item.toPriceEntities())
            }

            override fun shouldFetch(data: DomainProduct?): Boolean {
                return forceRefresh || data == null
            }

            override fun loadFromDb(): LiveData<DomainProduct> {
                return database.productEntityDao().getProduct(productId)
            }

            override fun createCall(): RetrofitCall<NetworkProduct> {
                return storeApiClient.getProductDetailV3(productId)
            }
        }.asLiveData()
    }
}
