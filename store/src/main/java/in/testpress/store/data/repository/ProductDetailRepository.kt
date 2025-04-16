package `in`.testpress.store.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.data.repository.BaseDetailRepository
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.network.Resource
import `in`.testpress.store.data.model.NetworkProduct
import `in`.testpress.store.data.model.toPriceEntities
import `in`.testpress.store.data.model.toProductEntity
import `in`.testpress.store.models.Order
import `in`.testpress.store.models.OrderItem
import `in`.testpress.store.network.StoreApiClient

class ProductDetailRepository(context: Context, private val productId: Int) :
    BaseDetailRepository<NetworkProduct, DomainProduct>(context) {

    private val apiClient = StoreApiClient(context)

    private val _orderStatus = MutableLiveData<Resource<Order>>()
    val orderStatus: LiveData<Resource<Order>> = _orderStatus

    private val _couponStatus = MutableLiveData<Resource<Order>>()
    val couponStatus: LiveData<Resource<Order>> = _couponStatus

    init {
        loadFromDatabase()
    }

    override suspend fun getFromDb(): DomainProduct? {
        return database.productEntityDao().getProduct(productId)
    }

    override suspend fun updateLiveDataFromDb() {
        _resource.postValue(Resource.success(getFromDb()))
    }

    override fun makeNetworkCall(callback: TestpressCallback<*>) {
        @Suppress("UNCHECKED_CAST")
        apiClient.getProductDetailV3(productId)
            .enqueue(callback as TestpressCallback<NetworkProduct>)
    }

    override suspend fun saveToDb(response: NetworkProduct) {
        database.productEntityDao().apply {
            insertProduct(response.toProductEntity())
            insertPrices(response.toPriceEntities())
        }
    }

    fun createOrder(orderItems: List<OrderItem>) {
        _orderStatus.postValue(Resource.loading(null))
        apiClient.order(orderItems).enqueue(object : TestpressCallback<Order>() {
            override fun onSuccess(result: Order?) {
                _orderStatus.postValue(Resource.success(result))
            }

            override fun onException(exception: TestpressException) {
                _orderStatus.postValue(Resource.error(exception, null))
            }
        })
    }

    fun applyCoupon(orderId: Long, couponCode: String) {
        _couponStatus.postValue(Resource.loading(null))
        apiClient.applyCoupon(orderId, couponCode).enqueue(object : TestpressCallback<Order>() {
            override fun onSuccess(result: Order?) {
                _couponStatus.postValue(Resource.success(result))
            }

            override fun onException(exception: TestpressException) {
                _couponStatus.postValue(Resource.error(exception, null))
            }
        })
    }

}