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

    fun createContentAttemptJSON(): String {
        return """
            {"id": 3790, "type": "attachment", "object_id": 450, 
            "object_url": "attachment", "trophies": "NA", "coins": "NA", 
            "assessment": null, "video": null, "content": null, 
            "attachment": {"id": 450, "user": {"id": 143, "url": 
            "https://sandbox.testpress.in/api/v2.3/users/143/", 
            "username": "testpress", "display_name": "Chikki Okay", 
            "first_name": "Chikki", "last_name": "Okay", 
            "photo": "https://media.testpress.in/i/3e9b9997677a469da3510fe27202882b.png", 
            "large_image": "https://media.testpress.in/i/2907ae35c591478e8dc272a66b12b5a2.jpg", 
            "medium_image": "https://media.testpress.in/i/c677fbc94a0d41d58815aa07c8756253.jpg", 
            "medium_small_image":
            "https://media.testpress.in/i/643ddd749154465599c519c36c6df349.jpg", 
            "small_image": "https://media.testpress.in/i/84a900af6a864494961a0931993d395f.jpg", 
            "x_small_image": "https://media.testpress.in/i/bd2450a5f45a4333adc3dc4f24f40fc3.jpg", 
            "mini_image": "https://media.testpress.in/i/f38f038c6c3d4fafb0a0eb51c21661a0.jpg"}, 
            "file_content": {"title": "Cohesion Example", "attachment_url": 
            "https://secure.testpress.in/institute/sandbox/21a32ff03f
            1a4880a7a5090955f2395b.png?token=BJflGEEd2EYH7lNFoFk0Gg&expires=1583312655", 
            "description": "", "id": 56}}, "wiziq": null, 
            "chapter_content": {"order": 6, "exam": null, "practice": null, 
            "html_content_title": null, "html_content_url": 
            "https://sandbox.testpress.in/api/v2.3/contents/238/html/", "url": 
            "https://sandbox.testpress.in/api/v2.3/contents/238/", "modified": "2020-02-17T09:53:26.640699Z",
            "attempts_url": "https://sandbox.testpress.in/api/v2.3/contents/238/attempts/", "chapter_id": 35,
            "chapter_slug": "chapter-2-1", "chapter_url":
            "https://sandbox.testpress.in/api/v2.3/chapters/chapter-2-1/", "id": 238, "video": null, "name":
            "Cohesion Example", "image": "https://media.testpress.in/static/img/fileicon.png", "attachment":
            {"title": "Cohesion Example", "attachment_url":
            "https://secure.testpress.in/institute/sandbox/21a32ff03f1a4880a7a5090955f2395b.png?token=BJflGEEd2EYH7lNFoFk0Gg&expires=1583312655",
            "description": "", "id": 56}, "description": "", "is_locked": false, "attempts_count": 254,
            "start": null, "end": null, "has_started": true, "bookmark_id": null, "comments_url":
            "https://sandbox.testpress.in/api/v2.3/contents/238/comments/", "wiziq": null}}
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

    @Test
    fun createAttempt() {
        val successResponse = MockResponse().setResponseCode(200).setBody(createContentAttemptJSON())
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.createContentAttempt(1).execute()
            mockWebServer.takeRequest()

            assertTrue(response.isSuccessful)
            assertEquals(3790, response.body().id)
        }
    }
}