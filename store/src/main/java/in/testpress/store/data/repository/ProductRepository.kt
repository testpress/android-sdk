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
import kotlinx.coroutines.withContext

class ProductRepository(val context: Context) {

    private val storeApiClient = StoreApiClient(context)
    private val database = TestpressDatabase.invoke(context)

    private var currentPage = 1
    private var isLoading = false
    private var hasNextPage = true
    private var lastFailedPage: Int? = null

    private var _productsResource: MutableLiveData<Resource<List<ProductLiteEntity>>> =
        MutableLiveData()
    val productsResource: LiveData<Resource<List<ProductLiteEntity>>>
        get() = _productsResource

    init {
        loadFromDatabase()
    }

    private fun loadFromDatabase() {
        _productsResource.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            val cachedProducts = database.productLiteEntityDao().getAll()
            if (cachedProducts.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    _productsResource.value = Resource.success(cachedProducts)
                }
            }
            fetchFromNetwork()
        }
    }

    fun fetchNextPage() {
        if (isLoading || !hasNextPage || lastFailedPage == currentPage) return
        currentPage++
        fetchFromNetwork()
    }

    fun retryNextPage() {
        fetchFromNetwork()
    }

    private fun fetchFromNetwork() {
        if (isLoading || !hasNextPage) return

        isLoading = true
        _productsResource.postValue(Resource.loading(null))

        val queryParams = hashMapOf<String, Any>("page" to currentPage)

        storeApiClient.getProductsV3(queryParams)
            .enqueue(object : TestpressCallback<NetworkProductListResponse>() {

                override fun onSuccess(result: NetworkProductListResponse) {
                    isLoading = false
                    hasNextPage = result.next != null
                    lastFailedPage = null
                    CoroutineScope(Dispatchers.IO).launch {
                        handleSuccessfulResponse(result)
                    }
                }

                override fun onException(exception: TestpressException?) {
                    isLoading = false
                    lastFailedPage = currentPage

                    val currentData = _productsResource.value?.data
                    _productsResource.postValue(Resource.error(exception!!, currentData))
                }
            })
    }

    private suspend fun handleSuccessfulResponse(response: NetworkProductListResponse) {
        if (currentPage == 1) {
            clearLocalProducts()
        }
        saveProductsToDatabase(response.results.products)
        updateProductsResourceFromDatabase()
    }

    private suspend fun clearLocalProducts() {
        database.productLiteEntityDao().deleteAll()
    }

    private suspend fun saveProductsToDatabase(products: List<NetworkProductLite>) {
        database.productLiteEntityDao().insertAll(products.asDomain())
    }

    private suspend fun updateProductsResourceFromDatabase() {
        val updatedProducts = database.productLiteEntityDao().getAll()
        _productsResource.postValue(Resource.success(updatedProducts))
    }

}
