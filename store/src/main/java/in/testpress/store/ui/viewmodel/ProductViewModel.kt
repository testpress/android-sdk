package `in`.testpress.store.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.testpress.database.entities.DomainProduct
import `in`.testpress.network.Resource
import `in`.testpress.store.data.repository.ProductRepository

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    fun loadProduct(
        productId: Int,
        forceRefresh: Boolean = false
    ): LiveData<Resource<DomainProduct>> {
        return repository.loadProduct(productId, forceRefresh)
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