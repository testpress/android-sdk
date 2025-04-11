package `in`.testpress.store.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.testpress.store.data.repository.ProductDetailRepository

class ProductViewModel(private val repository: ProductDetailRepository) : ViewModel() {

    val product = repository.resource

    fun refresh() {
        repository.loadFromDatabase()
    }

    fun retry() {
        repository.retry()
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