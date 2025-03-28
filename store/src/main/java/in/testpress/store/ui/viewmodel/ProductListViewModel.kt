package `in`.testpress.store.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import `in`.testpress.store.data.database.model.ProductLiteEntity
import `in`.testpress.store.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductListViewModel(private val repository: ProductRepository) : ViewModel() {

    val products = repository.productsResource

    fun fetchNextPage() {
        repository.fetchNextPage()
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