package `in`.testpress.exam.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test

class CommentApiClientTest: NetworkTestMixin() {

    private val commentsJson = getResponseFromFile("comments.json")

    @Test
    fun successNetworkCallShouldReturnSuccessResponse() {
        val successResponse = MockResponse().setResponseCode(200).setBody(commentsJson)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.getComments("url").execute()
            mockWebServer.takeRequest()

            Assert.assertTrue(response.isSuccessful)
        }
    }

    @Test
    fun networkFailureShouldReturnException() {
        val failureResponse = MockResponse().setStatus("Exception").setResponseCode(404).setBody(commentsJson)
        mockWebServer.enqueue(failureResponse)

        runBlocking {
            val response = service.getComments("url").execute()
            mockWebServer.takeRequest()

            Assert.assertEquals(404, response.code())
            Assert.assertEquals("HTTP/1.1 404 Client Error", failureResponse.status)
        }
    }

    @Test
    fun getCommentsShouldFetchComments() {
        val successResponse = MockResponse().setResponseCode(200).setBody(commentsJson)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.getComments("url").execute()
            mockWebServer.takeRequest()

            Assert.assertEquals("Hey", response.body().results?.get(0)?.comment)
            Assert.assertEquals("may 05 2020", response.body().results?.get(0)?.submitDate)
        }
    }
}
