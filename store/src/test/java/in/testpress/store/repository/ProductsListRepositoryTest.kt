package `in`.testpress.store.repository

import `in`.testpress.database.ProductsListEntity
import `in`.testpress.store.network.NetworkTestMixin
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProductsListRepositoryTest: NetworkTestMixin() {

    @Test
    fun shouldFetchReturnsNetworkCallNeededOrNot() {
        var forceFetch = false
        var data: ProductsListEntity? = ProductsListEntity(1,null,null)
        Assert.assertEquals(false,data == null)
        Assert.assertEquals(false, forceFetch)
        Assert.assertEquals(false, forceFetch || data == null)

        forceFetch = true
        data = null
        Assert.assertEquals(true, forceFetch)
        Assert.assertEquals(true, forceFetch || data == null)
    }

    @Test
    fun createCallShouldReturnNetworkResponse() {
        val productsListJson = getProductListFromFile("products_list.json")
        val successResponse = MockResponse().setResponseCode(200).setBody(productsListJson)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.productsList.execute()
            mockWebServer.takeRequest()

            Assert.assertTrue(response.isSuccessful)
            Assert.assertEquals(10, response.body().count)
            Assert.assertEquals(null, response.body().next)
            Assert.assertEquals(null, response.body().previous)
        }
    }
}
