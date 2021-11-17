package `in`.testpress.store.repository

import `in`.testpress.database.ProductCourseEntity
import `in`.testpress.database.ProductDao
import `in`.testpress.database.ProductPriceEntity
import `in`.testpress.database.ProductWithCoursesAndPrices
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.store.domain.DomainProductWithCoursesAndPrices
import `in`.testpress.store.domain.asDomainContent
import `in`.testpress.store.network.NetworkProductResponse
import `in`.testpress.store.network.StoreApiClient
import `in`.testpress.store.network.asDatabaseModel
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

open class ProductsRepository @Inject constructor(
        val productDao: ProductDao,
        val storeApiClient: StoreApiClient
) {

    fun fetch(forceFetch: Boolean = true): LiveData<Resource<List<DomainProductWithCoursesAndPrices>>> {
        return object : NetworkBoundResource<List<DomainProductWithCoursesAndPrices>, NetworkProductResponse>() {
            override fun saveNetworkResponseToDB(item: NetworkProductResponse) {
                saveProducts(item)
                saveCourses(item)
                savePrices(item)
                saveRelationalData(item)
            }

            override fun shouldFetch(data: List<DomainProductWithCoursesAndPrices>?): Boolean {
                return forceFetch || data.isNullOrEmpty()
            }

            override fun loadFromDb(): LiveData<List<DomainProductWithCoursesAndPrices>> {
                val liveData = MutableLiveData<List<DomainProductWithCoursesAndPrices>>()
                liveData.postValue(getProductFromDB()?.asDomainContent())
                return liveData
            }

            override fun createCall(): RetrofitCall<NetworkProductResponse> {
                return storeApiClient.productsList
            }

        }.asLiveData()
    }

    private fun saveProducts(item: NetworkProductResponse) {
        item.products?.forEach {
            it?.asDatabaseModel()?.let { it1 -> productDao.insertProduct(it1) }
        }
    }

    private fun saveCourses(item: NetworkProductResponse) {
        item.courses?.forEach {
            it?.asDatabaseModel()?.let { it1 -> productDao.insertCourse(it1) }
        }
    }

    private fun savePrices(item: NetworkProductResponse) {
        item.prices?.forEach {
            it?.asDatabaseModel()?.let { it1 -> productDao.insertPrice(it1) }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun saveRelationalData(item: NetworkProductResponse) {
        item.products?.forEach { product ->
            product?.courses?.forEach { courseId ->
                productDao.insert(ProductCourseEntity(product.id, courseId))
            }
            product?.prices?.forEach { priceId ->
                productDao.insertProductPrice(ProductPriceEntity(product.id, priceId))
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getProductFromDB(): List<ProductWithCoursesAndPrices>? = runBlocking {
        val job = CoroutineScope(Dispatchers.IO).async {
            productDao.getAll()
        }
        return@runBlocking job.await()
    }
}
