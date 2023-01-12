package `in`.testpress.store

import `in`.testpress.store.repository.ProductCategoriesRepository
import androidx.lifecycle.ViewModel

class ProductCategoriesViewModel(val repository: ProductCategoriesRepository): ViewModel() {
    val items = repository.resourceProductCategories

    fun loadContents() {
        return repository.loadItems()
    }
}