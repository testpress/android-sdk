package `in`.testpress.store.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.testpress.store.data.repository.ProductRepository


class ProductListViewModel(private val repository: ProductRepository) : ViewModel() {

    val products = repository.productsResource

    fun fetchNextPage() {
        repository.fetchNextPage()
    }

    fun retryNextPage() {
        repository.retryNextPage()
    }

    companion object {
        fun init(context: FragmentActivity): ProductListViewModel {
            return ViewModelProvider(context, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProductListViewModel(
                        ProductRepository(context)
                    ) as T
                }
            }).get(ProductListViewModel::class.java)
        }
    }
}