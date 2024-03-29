package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.ProductCategoryEntity
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.Resource
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProductCategoriesRepository(val context: Context) {
    private val courseService = CourseNetwork(context)
    private var productCategoryDao  = TestpressDatabase.invoke(context).productCategoryDao()
    var page = 1
    private var _resourceProductCategories: MutableLiveData<Resource<MutableList<ProductCategoryEntity>>> = MutableLiveData()
    val resourceProductCategories: LiveData<Resource<MutableList<ProductCategoryEntity>>>
        get() = _resourceProductCategories

    fun fetchCategories(page: Int = 1) {
        val queryParams = hashMapOf<String, Any>("page" to page)
        courseService.getProductsCategories(queryParams).enqueue(object :
            TestpressCallback<ApiResponse<List<ProductCategoryEntity>>>() {
            override fun onSuccess(result: ApiResponse<List<ProductCategoryEntity>>) {
                handleFetchSuccess(result)
            }

            override fun onException(exception: TestpressException?) {
                val contents = getAllProductCategories()
                if (contents.isNotEmpty()) {
                    _resourceProductCategories.postValue(Resource.error(exception!!, contents))
                } else {
                    _resourceProductCategories.postValue(Resource.error(exception!!, null))
                }
            }
        })
    }

    private fun handleFetchSuccess(response: ApiResponse<List<ProductCategoryEntity>>) {
        CoroutineScope(Dispatchers.IO).launch {
            if (page == 1) {
                deleteExistingProductCategories()
            }
            storeProductCategories(response.results as MutableList<ProductCategoryEntity>)
            _resourceProductCategories.postValue(Resource.success(getAllProductCategories()))

            if (response.next != null) {
                page += 1
                fetchCategories(page)
            } else {
                page = 1
            }
        }
    }

    private fun  getAllProductCategories():MutableList<ProductCategoryEntity>{
        return runBlocking(Dispatchers.IO) {
            productCategoryDao.getAll()
        }
    }

    private suspend fun storeProductCategories(response: MutableList<ProductCategoryEntity>) {
        productCategoryDao.insert(ProductCategoryEntity(id = -1, name = "All Product", null))
        productCategoryDao.insertAll(response)
    }

    private suspend fun deleteExistingProductCategories() {
        productCategoryDao.deleteAll()
    }
}