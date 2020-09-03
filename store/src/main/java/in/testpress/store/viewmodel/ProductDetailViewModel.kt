package `in`.testpress.store.viewmodel

import `in`.testpress.database.ProductDetailEntity
import `in`.testpress.network.Resource
import `in`.testpress.store.repository.ProductDetailRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel


class ProductDetailViewModel(val repository: ProductDetailRepository): ViewModel() {

    fun loadProductsList(forceFetch: Boolean, productSlug: String): LiveData<Resource<ProductDetailEntity>> {
        return repository.fetch(forceFetch, productSlug)
    }
}