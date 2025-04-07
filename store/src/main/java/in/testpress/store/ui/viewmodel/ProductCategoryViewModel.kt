package `in`.testpress.store.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.testpress.store.data.repository.ProductCategoryRepository

class ProductCategoryViewModel(private val repository: ProductCategoryRepository) : ViewModel() {

    val categories = repository.resource

    fun fetchNextPage() {
        repository.fetchNextPage()
    }

    fun retryNextPage() {
        repository.retryNextPage()
    }

    companion object {
        fun init(context: FragmentActivity): ProductCategoryViewModel {
            return ViewModelProvider(context, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProductCategoryViewModel(
                        ProductCategoryRepository(context)
                    ) as T
                }
            }).get(ProductCategoryViewModel::class.java)
        }
    }
}