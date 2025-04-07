package `in`.testpress.store.data.repository

import android.content.Context
import android.util.Log
import `in`.testpress.core.TestpressCallback
import `in`.testpress.data.repository.BasePaginatedRepository
import `in`.testpress.database.entities.ProductCategoryEntity
import `in`.testpress.database.entities.ProductLiteEntity
import `in`.testpress.network.Resource
import `in`.testpress.store.data.model.NetworkProductCategory
import `in`.testpress.store.data.model.asDomain
import `in`.testpress.store.network.StoreApiClient
import `in`.testpress.v2_4.models.ApiResponse

class ProductCategoryRepository(context: Context) :
    BasePaginatedRepository<ApiResponse<List<NetworkProductCategory>>, ProductCategoryEntity>(context) {

    private val apiClient = StoreApiClient(context)


    init {
        loadFromDatabase()
    }

    override suspend fun getFromDb(): List<ProductCategoryEntity>{
        val data = database.productCategoryDao().getAll()
        if (data.isNotEmpty()){
            data.add(0, ProductCategoryEntity(id = -1, name = "All Products"))
        }
        return data
    }

    override suspend fun clearLocalDb() {
        database.productCategoryDao().deleteAll()
    }

    override suspend fun saveToDb(response: ApiResponse<List<NetworkProductCategory>>) {
        database.productCategoryDao().insertAll(response.results.asDomain())
    }

    override suspend fun updateLiveDataFromDb() {
        _resource.postValue(Resource.success(getFromDb()))
    }

    override fun makeNetworkCall(queryParams: Map<String, Any>, callback: TestpressCallback<*>) {
        @Suppress("UNCHECKED_CAST")
        apiClient.getProductsCategories(queryParams)
            .enqueue(callback as TestpressCallback<ApiResponse<List<NetworkProductCategory>>>)
    }

    override fun extractNextPageAvailable(response: Any): Boolean {
        return (response as ApiResponse<*>).next != null
    }
}
