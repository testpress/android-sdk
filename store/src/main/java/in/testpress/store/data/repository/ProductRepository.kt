package `in`.testpress.store.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
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
    val contentDao = TestpressSDKDatabase.getContentDao(context)
    val chapterDao = TestpressSDKDatabase.getChapterDao(context)
    val storeApiClient = StoreApiClient(context)
    val database = TestpressDatabase.invoke(context)
    var page = 1
    val category = -1


    private var _productsResource: MutableLiveData<Resource<List<ProductLiteEntity>>> = MutableLiveData()
    val productsResource: LiveData<Resource<List<ProductLiteEntity>>>
        get() = _productsResource

    init {
        loadItems()
    }

    private fun loadItems() {
        _productsResource.postValue(Resource.loading(null))
        val queryParams = hashMapOf<String, Any>("page" to page)
        storeApiClient.getProductsV3(queryParams)
            .enqueue(object : TestpressCallback<NetworkProductListResponse>(){
                override fun onSuccess(result: NetworkProductListResponse) {
                    handleFetchSuccess(result)
                    updateProductsResource()
                }

                override fun onException(exception: TestpressException?) {
                    _productsResource.postValue(Resource.error(exception!!, null))
                }
            })
    }

    fun fetchNextPage() {
        page++
        loadItems()
    }

    private fun handleFetchSuccess(response: NetworkProductListResponse) {
        if (page == 1 && category == -1) {
            // Delete all contents of chapter once first page is fetched
            //deleteExistingProducts()
        }

        storeContent(response.results.products)
    }

    private fun deleteExistingProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            database.productLiteEntityDao().deleteAll()
        }
    }

    private fun updateProductsResource() {
        CoroutineScope(Dispatchers.IO).launch {
            val products = database.productLiteEntityDao().getAll()
            _productsResource.postValue(Resource.success(products))
        }
    }

    private fun storeContent(response: List<NetworkProductLite>) {
        CoroutineScope(Dispatchers.IO).launch {
            database.productLiteEntityDao().insertAll(response.asDomain())
        }
    }

}

