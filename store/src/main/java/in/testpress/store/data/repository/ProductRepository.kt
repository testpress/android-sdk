package `in`.testpress.store.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.network.NetworkBoundResource
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import `in`.testpress.store.data.model.NetworkProduct
import `in`.testpress.store.network.StoreApiClient

open class ProductRepository(
    val context: Context
) {
    protected val database = TestpressDatabase.invoke(context.applicationContext)
    private val storeApiClient = StoreApiClient(context)

    fun loadProduct(
        productId: Long,
        forceRefresh: Boolean = false
    ): LiveData<Resource<DomainProduct>> {
        return object : NetworkBoundResource<DomainProduct, NetworkProduct>() {
            override fun saveNetworkResponseToDB(item: NetworkProduct) {
                // TODO: saveNetworkResponseToDB
            }

            override fun shouldFetch(data: DomainProduct?): Boolean {
                // TODO: shouldFetch
                return false
            }

            override fun loadFromDb(): LiveData<DomainProduct> {
                // TODO: loadFromDb
                return MutableLiveData()
            }

            override fun createCall(): RetrofitCall<NetworkProduct> {
                // TODO: createCall
                return storeApiClient.getNetworkContent(contentUrl)
            }
        }.asLiveData()
    }
}
