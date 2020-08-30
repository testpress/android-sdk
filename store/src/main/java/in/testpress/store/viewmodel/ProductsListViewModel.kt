package `in`.testpress.store.viewmodel

import `in`.testpress.database.ProductsListEntity
import `in`.testpress.network.Resource
import `in`.testpress.store.repository.ProductsListRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ProductsListViewModel(val repository: ProductsListRepository): ViewModel() {

    fun loadProductsList(): LiveData<Resource<ProductsListEntity>> {
        return repository.fetch()
    }
}