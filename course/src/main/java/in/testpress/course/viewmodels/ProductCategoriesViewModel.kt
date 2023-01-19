package `in`.testpress.course.viewmodels

import `in`.testpress.course.repository.ProductCategoriesRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProductCategoriesViewModel(val repository: ProductCategoriesRepository): ViewModel() {
    val items = repository.resourceProductCategories

    fun loadCategories() {
        return repository.loadCategories()
    }
}

class ProductCategoriesViewModelFactory(private val repository: ProductCategoriesRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ProductCategoriesViewModel::class.java)){
            ProductCategoriesViewModel(repository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}