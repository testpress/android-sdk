package `in`.testpress.store.viewmodel

import `in`.testpress.database.ProductsListEntity
import `in`.testpress.network.Resource
import `in`.testpress.store.repository.ProductsListRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ProductsListViewModel(private val productsListRepository: ProductsListRepository): ViewModel() {

    fun load(forceFetch: Boolean): LiveData<Resource<ProductsListEntity?>> {
        return productsListRepository.fetch(forceFetch)
    }
}
