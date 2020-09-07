package `in`.testpress.store.repository

import `in`.testpress.database.ProductDetailEntity
import `in`.testpress.store.network.NetworkTestMixin
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test

class ProductDetailRepositoryTest: NetworkTestMixin() {

    @Test
    fun shouldFetchReturnsNetworkCallNeededOrNot() {
        var forceFetch = false
        var data: ProductDetailEntity? = ProductDetailEntity(1, null, image = null, title = "abc")
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
        val productsDetailJson = getProductListFromFile("product_detail.json")
        val successResponse = MockResponse().setResponseCode(200).setBody(productsDetailJson)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.getProductDetail("Soft-Skill").execute()
            mockWebServer.takeRequest()

            Assert.assertTrue(response.isSuccessful)
            Assert.assertEquals("94860.00", response.body().order?.amount)
            Assert.assertEquals("price", response.body().prices?.get(0)?.name)
        }
    }
}
