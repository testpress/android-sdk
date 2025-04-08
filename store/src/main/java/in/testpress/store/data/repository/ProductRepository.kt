package `in`.testpress.store.data.repository

import android.content.Context
import `in`.testpress.core.TestpressCallback
import `in`.testpress.data.repository.BasePaginatedRepository
import `in`.testpress.database.entities.ProductLiteEntity
import `in`.testpress.network.Resource
import `in`.testpress.store.data.model.NetworkProductListResponse
import `in`.testpress.store.data.model.asDomain
import `in`.testpress.store.network.StoreApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ProductRepository(
    context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) :
    BasePaginatedRepository<NetworkProductListResponse, ProductLiteEntity>(context, scope) {

    private val apiClient = StoreApiClient(context)

    init {
        loadFromDatabase()
    }

    override suspend fun getFromDb() = database.productLiteEntityDao().getAll()

    override suspend fun clearLocalDb() {
        database.productLiteEntityDao().deleteAll()
    }

    override suspend fun saveToDb(response: NetworkProductListResponse) {
        database.productLiteEntityDao().insertAll(response.results.products.asDomain())
    }

    override suspend fun updateLiveDataFromDb() {
        _resource.postValue(Resource.success(getFromDb()))
    }

    override fun makeNetworkCall(queryParams: Map<String, Any>, callback: TestpressCallback<*>) {
        @Suppress("UNCHECKED_CAST")
        apiClient.getProductsV3(queryParams)
            .enqueue(callback as TestpressCallback<NetworkProductListResponse>)
    }

    override fun extractNextPageAvailable(response: Any): Boolean {
        return (response as NetworkProductListResponse).next != null
    }
}

