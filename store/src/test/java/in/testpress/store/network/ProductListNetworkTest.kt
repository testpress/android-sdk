package `in`.testpress.store.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ProductListNetworkTest : NetworkTestMixin() {

    private val productsListJson = getResponseFromFile("products_list.json")

    @Test
    fun successNetworkCallShouldReturnSuccessResponse() {
        val successResponse = MockResponse().setResponseCode(200).setBody(productsListJson)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.productsList.execute()
            mockWebServer.takeRequest()

            Assert.assertTrue(response.isSuccessful)
        }
    }

    @Test
    fun networkFailureShouldReturnException() {
        val failureResponse = MockResponse().setStatus("Exception").setResponseCode(404).setBody(productsListJson)
        mockWebServer.enqueue(failureResponse)

        runBlocking {
            val response = service.productsList.execute()
            mockWebServer.takeRequest()

            Assert.assertEquals(404, response.code())
            Assert.assertEquals("HTTP/1.1 404 Client Error", failureResponse.status)
        }
    }

    @Test
    fun successNetworkCallShouldReturnCorrectProductsList() {
        val successResponse = MockResponse().setResponseCode(200).setBody(productsListJson)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.productsList.execute()
            mockWebServer.takeRequest()

            Assert.assertEquals(10, response.body().count)
        }
    }
}
