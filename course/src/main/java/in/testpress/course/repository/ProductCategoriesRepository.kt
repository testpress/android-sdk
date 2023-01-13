package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.api.CourseService
import `in`.testpress.course.models.ProductCategories
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.Resource
import `in`.testpress.store.network.StoreApiClient
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ProductCategoriesRepository(val context: Context) {
    private val courseService = CourseNetwork(context)
    var page = 1
    private var productCategoriesList = mutableListOf<ProductCategories>()
    private var _resourceProductCategories: MutableLiveData<Resource<MutableList<ProductCategories>>> = MutableLiveData()
    val resourceProductCategories: LiveData<Resource<MutableList<ProductCategories>>>
        get() = _resourceProductCategories

    fun loadItems(page: Int = 1) {
        courseService.getProductsCategories().enqueue(object :
            TestpressCallback<TestpressApiResponse<ProductCategories>>() {
            override fun onSuccess(result: TestpressApiResponse<ProductCategories>) {
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

    private fun handleFetchSuccess(response: TestpressApiResponse<ProductCategories>) {
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

    fun getAll():MutableList<ProductCategories>{
        return productCategoriesList
    }

    private fun storeContent(response: MutableList<ProductCategories>) {
        for (item in response){
            if (item !in productCategoriesList){
                productCategoriesList.add(item)
            }
        }
    }

    private fun deleteExistingContents() {
        productCategoriesList = mutableListOf()
    }
}