package `in`.testpress.store.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.store.data.repository.ProductDetailRepository
import `in`.testpress.store.models.Order
import `in`.testpress.store.models.OrderItem
import kotlin.Int

class ProductViewModel(private val repository: ProductDetailRepository) : ViewModel() {

    val product = repository.resource
    val order = repository.orderStatus
    val coupon = repository.couponStatus

    var amountSaved = 100.00
    var currentCoupon = ""

    private val _isCouponApplied = MutableLiveData<Boolean>(false)
    val isCouponApplied: LiveData<Boolean> get() = _isCouponApplied

    fun applyCoupon() {
        _isCouponApplied.value = true
    }

    fun removeCoupon() {
        _isCouponApplied.value = false
    }

    fun refresh() {
        repository.loadFromDatabase()
    }

    fun retry() {
        repository.retry()
    }

    fun createOrder(product: DomainProduct?) {
        product ?: return
        val orderItem = createOrderItem(product)
        val orderItems: MutableList<OrderItem> = ArrayList()
        orderItems.add(orderItem)
        repository.createOrder(orderItems)
    }

    fun applyCoupon(orderId: Long, couponString: String){
        repository.applyCoupon(orderId, couponString)
    }

    private fun createOrderItem(product: DomainProduct): OrderItem {
        val price = product.prices.find { it.price == product.product.price }
        val orderItem = OrderItem()
        orderItem.product = product.product.slug
        orderItem.quantity = 1
        orderItem.price = price?.id.toString()
        return orderItem
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel the repository's scope when the ViewModel is cleared
        repository.cancelScope()
    }

    companion object {
        fun init(context: FragmentActivity, productId: Int): ProductViewModel {
            return ViewModelProvider(context, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProductViewModel(
                        ProductDetailRepository(context, productId)
                    ) as T
                }
            }).get(ProductViewModel::class.java)
        }
    }
}