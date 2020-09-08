package `in`.testpress.store.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProductListNetworkTest: NetworkTestMixin() {

    @Test
    fun networkCallShouldReturnFetchedData() {
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
