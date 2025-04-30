package `in`.testpress.store.data.repository

import android.content.Context
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.data.repository.BasePaginatedRepository
import `in`.testpress.database.entities.ProductLiteEntity
import `in`.testpress.models.greendao.Course
import `in`.testpress.network.Resource
import `in`.testpress.store.data.model.NetworkProductListResponse
import `in`.testpress.store.data.model.asDomain
import `in`.testpress.store.network.StoreApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ProductListRepository(
    val context: Context,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) :
    BasePaginatedRepository<NetworkProductListResponse, ProductLiteEntity>(context, scope) {

    private val apiClient = StoreApiClient(context)
    private var categoryId = -1

    init {
        loadFromDatabase()
    }

    fun setCategoryId(categoryId: Int) {
        this.categoryId = categoryId
    }

    override suspend fun getFromDb(): List<ProductLiteEntity> {
        return if (categoryId == -1) {
            database.productLiteEntityDao().getAll()
        } else {
            database.productLiteEntityDao().getByCategoryId(categoryId)
        }
    }

    override suspend fun clearLocalDb() {
        if (categoryId == -1) {
            database.productLiteEntityDao().deleteAll()
        } else {
            database.productLiteEntityDao().deleteByCategoryId(categoryId)
        }
    }

    override suspend fun saveToDb(response: NetworkProductListResponse) {
        database.productLiteEntityDao().insertAll(response.results.products.asDomain())
        val courseDao = TestpressSDKDatabase.getCourseDao(context)
        courseDao.insertOrReplaceInTx(response.results.courses.asDomain())
    }

    override suspend fun updateLiveDataFromDb() {
        _resource.postValue(Resource.success(getFromDb()))
    }

    override fun makeNetworkCall(queryParams: Map<String, Any>, callback: TestpressCallback<*>) {
        val updatedParams = queryParams.toMutableMap()

        if (categoryId != -1) {
            updatedParams["category"] = categoryId
        }

        @Suppress("UNCHECKED_CAST")
        apiClient.getProductsV3(updatedParams)
            .enqueue(callback as TestpressCallback<NetworkProductListResponse>)
    }

    override fun extractNextPageAvailable(response: Any): Boolean {
        return (response as NetworkProductListResponse).next != null
    }
}

