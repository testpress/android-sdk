package `in`.testpress.store.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@Ignore("MockWebServer 3.10.0 incompatible with OkHttp 4 runtime")
@RunWith(JUnit4::class)
class ProductListNetworkTest : NetworkTestMixin() {

    @Test
    fun successNetworkCallShouldReturnSuccessResponse() {
        val successResponse = MockResponse().setResponseCode(200).setBody("{\"count\":0,\"results\":[]}")
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.getProducts(emptyMap()).execute()
            mockWebServer.takeRequest()

            Assert.assertTrue(response.isSuccessful)
        }
    }

    @Test
    fun networkFailureShouldReturnException() {
        val failureResponse = MockResponse().setStatus("Exception").setResponseCode(404).setBody("{}")
        mockWebServer.enqueue(failureResponse)

        runBlocking {
            val response = service.getProducts(emptyMap()).execute()
            mockWebServer.takeRequest()

            Assert.assertEquals(404, response.code())
        }
    }
}
