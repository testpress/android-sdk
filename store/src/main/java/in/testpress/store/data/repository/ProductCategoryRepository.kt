package `in`.testpress.store.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.ProductCategoryEntity
import `in`.testpress.network.Resource
import `in`.testpress.store.data.model.NetworkProductCategory
import `in`.testpress.store.data.model.asDomain
import `in`.testpress.store.network.StoreApiClient
import `in`.testpress.v2_4.models.ApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductCategoryRepository(val context: Context) {

    private val storeApiClient = StoreApiClient(context)
    private val database = TestpressDatabase.invoke(context)

    private var currentPage = 1
    private var isLoading = false
    private var hasNextPage = true
    private var lastFailedPage: Int? = null

    private var _categoriesResource: MutableLiveData<Resource<List<ProductCategoryEntity>>> =
        MutableLiveData()
    val categoriesResource: LiveData<Resource<List<ProductCategoryEntity>>>
        get() = _categoriesResource

    init {
        loadFromDatabase()
    }

    private fun loadFromDatabase() {
        _categoriesResource.value = Resource.loading(null)
        CoroutineScope(Dispatchers.IO).launch {
            val cachedCategories = database.productCategoryDao().getAll()
            if (cachedCategories.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    _categoriesResource.value = Resource.success(cachedCategories)
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
        _categoriesResource.postValue(Resource.loading(null))

        val queryParams = hashMapOf<String, Any>("page" to currentPage)

        storeApiClient.getProductsCategories(queryParams)
            .enqueue(object : TestpressCallback<ApiResponse<List<NetworkProductCategory>>>() {

                override fun onSuccess(result: ApiResponse<List<NetworkProductCategory>>) {
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

                    val currentData = _categoriesResource.value?.data
                    _categoriesResource.postValue(Resource.error(exception!!, currentData))
                }
            })
    }

    private suspend fun handleSuccessfulResponse(response: ApiResponse<List<NetworkProductCategory>>) {
        if (currentPage == 1) {
            clearLocalProducts()
        }
        saveProductsToDatabase(response.results.asDomain())
        updateProductsResourceFromDatabase()
    }

    private suspend fun clearLocalProducts() {
        database.productCategoryDao().deleteAll()
    }

    private suspend fun saveProductsToDatabase(category: List<ProductCategoryEntity>) {
        database.productCategoryDao().insertAll(category)
    }

    private suspend fun updateProductsResourceFromDatabase() {
        val updatedProducts = database.productCategoryDao().getAll()
        _categoriesResource.postValue(Resource.success(updatedProducts))
    }

}