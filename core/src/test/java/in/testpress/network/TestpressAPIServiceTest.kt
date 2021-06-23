package `in`.testpress.network

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TestpressAPIServiceTest: NetworkTestMixin() {
    private val forumJSON = getResponseFromFile("discussions.json")

    @Test
    fun successNetworkCallShouldReturn() {
        val successResponse = MockResponse().setResponseCode(200).setBody(forumJSON)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.getDiscussions(hashMapOf()).body()
            mockWebServer.takeRequest()

            print("Response : ${response}")
//            Assert.assertTrue(response.)
        }
    }
}