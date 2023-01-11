package `in`.testpress.store

import `in`.testpress.store.repository.ProductCategoriesRepository

class ProductCategoriesViewModel(val repository: ProductCategoriesRepository) {
    val items = repository.resourceProductCategories

    fun loadContents() {
        return repository.loadItems()
    }
}