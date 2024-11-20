package `in`.testpress.store.viewmodel

import `in`.testpress.database.ProductDao
import `in`.testpress.store.network.StoreApiClient
import `in`.testpress.store.repository.ProductsRepository
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class ProductListViewModelTest {

    lateinit var viewModel: ProductListViewModel

    @Mock
    lateinit var productDao: ProductDao

    @Mock
    lateinit var storeApiClient: StoreApiClient

    @Mock
    lateinit var context: Context

    private lateinit var repository: ProductsRepository

    private lateinit var spy: ProductsRepository

    private lateinit var viewModelSpy: ProductListViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        repository = ProductsRepository(productDao, storeApiClient)
        viewModel = ProductListViewModel(repository, SavedStateHandle())
        spy = spy(repository)

    }

    @Test
    fun testGetProductsCallsRepository() {
        viewModelSpy = spy(viewModel)
        viewModelSpy.get()

        verify(spy).fetch()
    }
}