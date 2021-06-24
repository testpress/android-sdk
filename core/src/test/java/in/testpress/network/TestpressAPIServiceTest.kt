package `in`.testpress.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TestpressAPIServiceTest: NetworkTestMixin() {

    fun getResponseFromFile(filename: String): String {
        return ClassLoader.getSystemResource(filename).readText()
    }

    @Test
    fun successNetworkCallShouldReturn() {
        val forumJSON = getResponseFromFile("discussions.json")
        val successResponse = MockResponse().setResponseCode(200).setBody(forumJSON)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.fetchDiscussions(hashMapOf())
            mockWebServer.takeRequest()

            Assert.assertTrue(response.isSuccessful)
        }
    }

    @Test
    fun networkFailureShouldReturnException() {
        val forumJSON = getResponseFromFile("discussions.json")
        val failureResponse = MockResponse().setStatus("Exception").setResponseCode(404).setBody(forumJSON)
        mockWebServer.enqueue(failureResponse)

        runBlocking {
            val response = service.fetchDiscussions(hashMapOf())
            mockWebServer.takeRequest()

            Assert.assertEquals(404, response.code())
            Assert.assertEquals("HTTP/1.1 404 Client Error", failureResponse.status)
        }
    }

    @Test
    fun fetchDiscussionsShouldFetchFromAPI() {
        val forumJSON = getResponseFromFile("discussions.json")
        val successResponse = MockResponse().setResponseCode(200).setBody(forumJSON)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.fetchDiscussions(hashMapOf())
            mockWebServer.takeRequest()

            Assert.assertEquals("Testing 10", response.body()?.results?.get(0)?.title)
        }
    }
}