package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.ProductCategoryEntity
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.Resource
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductCategoriesRepository(val context: Context) {
    private val courseService = CourseNetwork(context)
    private var productCategoryDao  = TestpressDatabase.invoke(context).productCategoryDao()
    var page = 1
    private var _resourceProductCategories: MutableLiveData<Resource<MutableList<ProductCategoryEntity>>> = MutableLiveData()
    val resourceProductCategories: LiveData<Resource<MutableList<ProductCategoryEntity>>>
        get() = _resourceProductCategories

    fun loadItems(page: Int = 1) {
        val queryParams = hashMapOf<String, Any>("page" to page)
        courseService.getProductsCategories(queryParams).enqueue(object :
            TestpressCallback<TestpressApiResponse<ProductCategoryEntity>>() {
            override fun onSuccess(result: TestpressApiResponse<ProductCategoryEntity>) {
                handleFetchSuccess(result)
            }

            override fun onException(exception: TestpressException?) {
                val contents = getAll()
                if (contents.isNotEmpty()) {
                    _resourceProductCategories.postValue(Resource.error(exception!!, contents))
                } else {
                    _resourceProductCategories.postValue(Resource.error(exception!!, null))
                }
            }
        })
    }

    private fun handleFetchSuccess(response: TestpressApiResponse<ProductCategoryEntity>) {
        CoroutineScope(Dispatchers.IO).launch {
            if (page == 1) {
                deleteExistingContents()
            }
            storeContent(response.results)
            _resourceProductCategories.postValue(Resource.success(getAll()))

            if (response.next != null) {
                page += 1
                loadItems(page)
            } else {
                page = 1
            }
        }
    }

    private fun  getAll():MutableList<ProductCategoryEntity>{
        return productCategoryDao.getAll()
    }

    private suspend fun storeContent(response: MutableList<ProductCategoryEntity>) {
        productCategoryDao.insert(ProductCategoryEntity(id = 0, name = "All Product", null))
        productCategoryDao.insertAll(response)
    }

    private suspend fun deleteExistingContents() {
        productCategoryDao.deleteAll()
    }
}