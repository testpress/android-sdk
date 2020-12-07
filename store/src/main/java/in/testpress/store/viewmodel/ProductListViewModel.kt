package `in`.testpress.store.viewmodel

import `in`.testpress.network.Resource
import `in`.testpress.store.domain.DomainProductWithCoursesAndPrices
import `in`.testpress.store.repository.ProductsRepository
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

open class ProductListViewModel @ViewModelInject constructor(
        private val productsRepository: ProductsRepository,
        @Assisted private val savedStateHandle: SavedStateHandle
): ViewModel() {

    fun get(): LiveData<Resource<List<DomainProductWithCoursesAndPrices>>> {
        return productsRepository.fetch()
    }
}
