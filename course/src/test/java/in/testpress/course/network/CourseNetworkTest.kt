package `in`.testpress.course.network

import `in`.testpress.network.ErrorHandlingCallAdapterFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class CourseNetworkTest {
    lateinit var mockWebServer: MockWebServer
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

    fun getContentJSON(): String {
        return """
            {"order":6,"exam":null,"html_content_title":null,"html_content_url":"https://sandbox.testpress.in/api/v2.3/contents/238/html/","free_preview":false,"url":"https://sandbox.testpress.in/api/v2.4/contents/238/","modified":"2020-02-17T09:53:26.640699Z","attempts_url":"https://sandbox.testpress.in/api/v2.3/contents/238/attempts/","chapter_id":35,"chapter_slug":"chapter-2-1","chapter_url":"https://sandbox.testpress.in/api/v2.4/chapters/chapter-2-1/","id":238,"video":null,"name":"Cohesion1 Example","image":"https://media.testpress.in/static/img/fileicon.png","attachment":{"title":"Cohesion1 Example","attachment_url":"https://secure.testpress.in/institute/sandbox/21a32ff03f1a4880a7a5090955f2395b.png?token=uFTQfTMVFe_B4a8VYJCdbw&expires=1582873751","description":"","id":56},"description":"","is_locked":false,"attempts_count":null,"start":null,"end":null,"has_started":true,"content_type":"Attachment","title":"Cohesion1 Example","bookmark_id":null,"html_content":null,"active":true,"comments_url":"https://sandbox.testpress.in/api/v2.3/contents/238/comments/","wiziq":null}
        """.trimIndent()
    }

    @Test
    fun testGetNetworkContent() {
        val successResponse = MockResponse().setResponseCode(200).setBody(getContentJSON())
        mockWebServer.enqueue(successResponse)
        runBlocking {
            val response = service.getNetworkContent("/content/2").execute()
            mockWebServer.takeRequest()
            assertEquals(response.body().title, "Cohesion1 Example")
        }
    }
}