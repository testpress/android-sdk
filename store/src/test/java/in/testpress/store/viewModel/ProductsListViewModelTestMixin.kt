package `in`.testpress.store.viewModel

import `in`.testpress.database.ProductsListEntity
import `in`.testpress.network.Resource
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import org.junit.Rule
import org.mockito.Mock
import org.mockito.Mockito.mock

open class ProductsListViewModelTestMixin {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    var repository: ProductsListRepository = mock(ProductsListRepository::class.java)

    abstract class ProductsListRepository {

        private lateinit var resource: Resource<ProductsListEntity>

        fun setResponse(resource: Resource<ProductsListEntity>) {
            this.resource = resource
        }

        fun fetch(): MutableLiveData<Resource<ProductsListEntity>> {
            fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }
            return MutableLiveData<Resource<ProductsListEntity>>().default(resource)
        }
    }
}