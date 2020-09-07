package `in`.testpress.store.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test

class ProductDetailNetworkTest: NetworkTestMixin() {
    @Test
    fun networkCallShouldReturnFetchedData() {
        val productsDetailJson = getProductListFromFile("product_detail.json")
        val successResponse = MockResponse().setResponseCode(200).setBody(productsDetailJson)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.getProductDetail("soft-skills").execute()
            mockWebServer.takeRequest()

            Assert.assertTrue(response.isSuccessful)
            Assert.assertEquals("soft-skills", response.body().slug)
            Assert.assertEquals(66, response.body().id)
            Assert.assertEquals("Soft Skills", response.body().title)
        }
    }
}
