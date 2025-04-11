package `in`.testpress.store.data.repository

import android.content.Context
import `in`.testpress.core.TestpressCallback
import `in`.testpress.data.repository.BaseDetailRepository
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.network.Resource
import `in`.testpress.store.data.model.NetworkProduct
import `in`.testpress.store.data.model.toPriceEntities
import `in`.testpress.store.data.model.toProductEntity
import `in`.testpress.store.network.StoreApiClient

class ProductDetailRepository(context: Context, private val productId: Int) :
    BaseDetailRepository<NetworkProduct, DomainProduct>(context) {

    private val apiClient = StoreApiClient(context)

    init {
        loadFromDatabase()
    }

    override suspend fun getFromDb(): DomainProduct? {
        return database.productEntityDao().getProduct(productId)
    }

    override suspend fun updateLiveDataFromDb() {
        _resource.postValue(Resource.success(getFromDb()))
    }

    override fun makeNetworkCall(callback: TestpressCallback<*>) {
        @Suppress("UNCHECKED_CAST")
        apiClient.getProductDetailV3(productId)
            .enqueue(callback as TestpressCallback<NetworkProduct>)
    }

    override suspend fun saveToDb(response: NetworkProduct) {
        database.productEntityDao().apply {
            insertProduct(response.toProductEntity())
            insertPrices(response.toPriceEntities())
        }
    }

}