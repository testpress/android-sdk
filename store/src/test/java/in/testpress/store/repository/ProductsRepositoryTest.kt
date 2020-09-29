package `in`.testpress.store.repository

import `in`.testpress.database.ProductDao
import `in`.testpress.database.ProductWithCoursesAndPrices
import `in`.testpress.network.Resource
import `in`.testpress.network.observeOnce
import `in`.testpress.store.domain.DomainProductWithCoursesAndPrices
import `in`.testpress.store.network.StoreApiClient
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

inline fun <reified T> mock(): T = Mockito.mock(T::class.java)

@RunWith(MockitoJUnitRunner::class)
class ProductsRepositoryTest {
    private val roomProductDao = mock(ProductDao::class.java)
    private val apiClient = mock(StoreApiClient::class.java)
    private val repo = ProductsRepository(roomProductDao, apiClient)
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun emptyDbFetchesDataFromNetwork() {
        var data = MutableLiveData<List<ProductWithCoursesAndPrices>>()
        `when`(roomProductDao.getAll()).thenReturn(data)
        val observer = mock<Observer<Resource<List<DomainProductWithCoursesAndPrices>>>>()
        data.value = emptyList()

        // db
        // db empty
        // network call
        // assert network call made
        runBlocking {
            repo.fetch().observeForever(observer)
            verify(apiClient).productsList
            verifyNoMoreInteractions(apiClient)
        }
    }
}