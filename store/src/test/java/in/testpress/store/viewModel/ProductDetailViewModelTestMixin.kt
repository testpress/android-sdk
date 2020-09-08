package `in`.testpress.store.viewModel

import `in`.testpress.database.ProductDetailEntity
import `in`.testpress.network.Resource
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import org.junit.Rule
import org.mockito.Mock
import org.mockito.Mockito

open class ProductDetailViewModelTestMixin {

    var isAccessCodeEnabled = false

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    var repository: ProductDetailRepository = Mockito.mock(ProductDetailRepository::class.java)

    abstract class ProductDetailRepository {

        private lateinit var resource: Resource<ProductDetailEntity>

        fun setResponse(resource: Resource<ProductDetailEntity>) {
            this.resource = resource
        }

        fun fetch(): MutableLiveData<Resource<ProductDetailEntity>> {
            fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }
            return MutableLiveData<Resource<ProductDetailEntity>>().default(resource)
        }
    }

    fun enableHaveAccessCode() {
        isAccessCodeEnabled = true
    }
}
