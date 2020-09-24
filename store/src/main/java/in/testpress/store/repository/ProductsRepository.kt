package `in`.testpress.store.repository

import `in`.testpress.database.TestpressDatabase
import `in`.testpress.network.Resource
import `in`.testpress.store.domain.DomainProductWithCoursesAndPrices
import `in`.testpress.store.network.StoreApiClient
import android.content.Context
import androidx.lifecycle.LiveData

open class ProductsRepository(val context: Context) {
    val roomProductDao = TestpressDatabase(context).productDao()
    val storeApiClient = StoreApiClient(context)

    fun fetch(forceRefresh: Boolean = false): LiveData<Resource<List<DomainProductWithCoursesAndPrices>>> {
        return ProductNetworkBoundResource(roomProductDao, storeApiClient, forceRefresh).asLiveData()
    }
}
