package `in`.testpress.store.repository

import `in`.testpress.database.ProductCourseEntity
import `in`.testpress.database.ProductDao
import `in`.testpress.database.ProductPriceEntity
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.network.observeOnce
import `in`.testpress.store.domain.DomainProductWithCoursesAndPrices
import `in`.testpress.store.domain.asDomainContent
import `in`.testpress.store.network.NetworkProductResponse
import `in`.testpress.store.network.StoreApiClient
import `in`.testpress.store.network.asDatabaseModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ProductNetworkBoundResource(val roomProductDao: ProductDao, val storeApiClient: StoreApiClient, val forceRefresh: Boolean) : NetworkBoundResource<List<DomainProductWithCoursesAndPrices>, NetworkProductResponse>() {
    override fun saveNetworkResponseToDB(item: NetworkProductResponse) {
        saveCourses(item)
        saveProducts(item)
        savePrices(item)
        saveRelationalData(item)
    }

    override fun loadFromDb(): LiveData<List<DomainProductWithCoursesAndPrices>> {
        val liveData = MutableLiveData<List<DomainProductWithCoursesAndPrices>>()
        roomProductDao.getAll().observeOnce {
            liveData.postValue(it.asDomainContent())
        }
        return liveData
    }

    override fun createCall(): RetrofitCall<NetworkProductResponse> {
        return storeApiClient.productsList
    }

    override fun shouldFetch(data: List<DomainProductWithCoursesAndPrices>?): Boolean {
        return forceRefresh || data.isNullOrEmpty() //TODO rate limit?!
    }

    private fun saveRelationalData(item: NetworkProductResponse) {
        item.products?.forEach { product ->
            product?.courses?.forEach { courseId ->
                roomProductDao.insert(ProductCourseEntity(product.id, courseId))
            }
            product?.prices?.forEach { priceId ->
                roomProductDao.insertProductPrice(ProductPriceEntity(product.id, priceId))
            }
        }
    }

    private fun savePrices(item: NetworkProductResponse) {
        item.prices?.forEach {
            it?.asDatabaseModel()?.let { it1 -> roomProductDao.insertPrice(it1) }
        }
    }

    private fun saveProducts(item: NetworkProductResponse) {
        item.products?.forEach {
            it?.asDatabaseModel()?.let { it1 -> roomProductDao.insertProduct(it1) }
        }
    }

    private fun saveCourses(item: NetworkProductResponse) {
        item.courses?.forEach {
            it?.asDatabaseModel()?.let { it1 -> roomProductDao.insertCourse(it1) }
        }
    }
}