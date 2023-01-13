package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.ProductCategoriesRepository
import androidx.lifecycle.ViewModel

class ProductCategoriesViewModel(val repository: ProductCategoriesRepository): ViewModel() {
    val items = repository.resourceProductCategories

    fun loadContents() {
        return repository.loadItems()
    }
}