package `in`.testpress.store.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.ProductLiteEntity
import `in`.testpress.network.Resource
import `in`.testpress.store.data.model.NetworkProductListResponse
import `in`.testpress.store.data.model.NetworkProductLite
import `in`.testpress.store.data.model.asDomain
import `in`.testpress.store.network.StoreApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductRepository(val context: Context) {
    val storeApiClient = StoreApiClient(context)
    val database = TestpressDatabase.invoke(context)
    var page = 1
    val category = -1
    private var isLoading = false
    private var hasNextPage = true

    private var _productsResource: MutableLiveData<Resource<List<ProductLiteEntity>>> = MutableLiveData()
    val productsResource: LiveData<Resource<List<ProductLiteEntity>>>
        get() = _productsResource

    init {
        loadFromDatabase()
    }

    private fun loadFromDatabase() {
        _productsResource.postValue(Resource.loading(null))
        CoroutineScope(Dispatchers.IO).launch {
            val cachedProducts = database.productLiteEntityDao().getAll()
            if (cachedProducts.isNotEmpty()) {
                _productsResource.postValue(Resource.success(cachedProducts))
            }
            fetchFromNetwork()
        }
    }

    private fun fetchFromNetwork() {
        if (isLoading || !hasNextPage) return
        isLoading = true

        val queryParams = hashMapOf<String, Any>("page" to page)
        storeApiClient.getProductsV3(queryParams)
            .enqueue(object : TestpressCallback<NetworkProductListResponse>(){
                override fun onSuccess(result: NetworkProductListResponse) {
                    CoroutineScope(Dispatchers.IO).launch {
                        handleFetchSuccess(result)
                        isLoading = false
                        hasNextPage = result.next != null
                    }
                }

                override fun onException(exception: TestpressException?) {
                    isLoading = false
                    if (_productsResource.value?.data.isNullOrEmpty()) {
                        _productsResource.postValue(Resource.error(exception!!, null))
                    }
                }
            })
    }

    fun fetchNextPage() {
        if (isLoading|| !hasNextPage) return
        page++
        fetchFromNetwork()
    }

    private suspend fun handleFetchSuccess(response: NetworkProductListResponse) {
        if (page == 1 && category == -1) {
            // Delete all Products once first page is fetched
            deleteExistingProducts()
        }

        storeContent(response.results.products)
    }

    private suspend fun deleteExistingProducts() {
        database.productLiteEntityDao().deleteAll()
        updateProductsResource()
    }

    private suspend fun updateProductsResource() {
        val products = database.productLiteEntityDao().getAll()
        if (products.isEmpty()) return
        _productsResource.postValue(Resource.success(products))
    }

    private suspend fun storeContent(response: List<NetworkProductLite>) {
        database.productLiteEntityDao().insertAll(response.asDomain())
        updateProductsResource()
    }

}

