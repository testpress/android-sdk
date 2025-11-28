package `in`.testpress.course.network

import `in`.testpress.network.ErrorHandlingCallAdapterFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.HashMap

@RunWith(JUnit4::class)
class BookmarksApiTest {
    private lateinit var mockWebServer: MockWebServer
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: CourseService

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(ErrorHandlingCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CourseService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun testGetBookmarks() {
        val bookmarksJson = """
            {
                "results": [
                    {
                        "id": 1,
                        "pageNumber": 1,
                        "previewText": "Bookmark preview"
                    }
                ]
            }
        """.trimIndent()
        
        val successResponse = MockResponse().setResponseCode(200).setBody(bookmarksJson)
        mockWebServer.enqueue(successResponse)
        
        runBlocking {
            val queryParams = hashMapOf<String, Any>(
                "content_type" to "chapter_content",
                "object_id" to 123L,
                "bookmark_type" to "annotate"
            )
            val response = service.getBookmarks(queryParams).execute()
            val request = mockWebServer.takeRequest()
            
            assertTrue(response.isSuccessful)
            assertEquals("/api/v3/bookmarks/", request.path.split("?")[0])
            assertTrue(request.path.contains("content_type=chapter_content"))
            assertTrue(request.path.contains("object_id=123"))
            assertEquals(1, response.body()?.results?.size)
        }
    }

    @Test
    fun testCreateBookmark() {
        val bookmarkJson = """
            {
                "id": 2,
                "pageNumber": 3,
                "previewText": "New bookmark"
            }
        """.trimIndent()
        
        val successResponse = MockResponse().setResponseCode(201).setBody(bookmarkJson)
        mockWebServer.enqueue(successResponse)
        
        runBlocking {
            val requestBody = hashMapOf<String, Any>(
                "content_type" to "chapter_content",
                "object_id" to 123L,
                "bookmark_type" to "annotate",
                "pageNumber" to 3,
                "previewText" to "New bookmark"
            )
            val response = service.createBookmark(requestBody).execute()
            val request = mockWebServer.takeRequest()
            
            assertTrue(response.isSuccessful)
            assertEquals("/api/v3/bookmarks/", request.path)
            assertEquals("POST", request.method)
            assertEquals(2L, response.body()?.id)
        }
    }

    @Test
    fun testDeleteBookmark() {
        val successResponse = MockResponse().setResponseCode(204)
        mockWebServer.enqueue(successResponse)
        
        runBlocking {
            val response = service.deleteBookmark(1L).execute()
            val request = mockWebServer.takeRequest()
            
            assertTrue(response.isSuccessful)
            assertEquals("/api/v3/bookmarks/1/", request.path)
            assertEquals("DELETE", request.method)
        }
    }
}

