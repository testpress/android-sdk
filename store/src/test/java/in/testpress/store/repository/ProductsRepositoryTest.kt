package `in`.testpress.store.repository

import `in`.testpress.database.ProductDao
import `in`.testpress.store.network.NetworkProductResponse
import `in`.testpress.store.network.StoreApiClient
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProductsRepositoryTest {

    @Mock
    lateinit var productDao: ProductDao

    @Mock
    lateinit var storeApiClient: StoreApiClient

    @Mock
    lateinit var context: Context

    private lateinit var repository: ProductsRepository

    private lateinit var spy: ProductsRepository

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        repository = ProductsRepository(productDao, storeApiClient)
        spy = Mockito.spy(repository)
    }

    @Test
    fun testWhenDbDataIsAvailableReturnDataImmediately() {
        repository.fetch(false)
        verify(spy).getProductFromDB()
    }

    @Test
    fun testWhenNetworkFetchedSaveResponseToDb() {
        repository.fetch(true)
        verify(spy).saveRelationalData(item = NetworkProductResponse())
    }

    @Test
    fun testWhenForceFetchFalseReturnDataImmediately() {
        repository.fetch(false)
        verify(spy).getProductFromDB()
    }
}
