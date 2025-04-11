package `in`.testpress.store.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.network.Resource
import `in`.testpress.store.data.repository.ProductRepository

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _product = MutableLiveData<LiveData<Resource<DomainProduct>>>()
    val product: LiveData<Resource<DomainProduct>> = Transformations.switchMap(_product) { it }

    fun load(productId: Int, forceRefresh: Boolean = false) {

        _product.value = liveData {
            Resource.loading(_product.value?.value?.data)
        }

        _product.value = repository.loadProduct(productId, forceRefresh)
    }

    companion object {
        fun init(context: FragmentActivity): ProductViewModel {
            return ViewModelProvider(context, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProductViewModel(
                        ProductRepository(context)
                    ) as T
                }
            }).get(ProductViewModel::class.java)
        }
    }
}