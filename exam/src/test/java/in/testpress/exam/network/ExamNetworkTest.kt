package `in`.testpress.exam.network

import `in`.testpress.network.ErrorHandlingCallAdapterFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.io.ByteStreams
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ExamNetworkTest {
    lateinit var mockWebServer: MockWebServer
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: ExamService

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addCallAdapterFactory(ErrorHandlingCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExamService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    fun getContentFromFile(filename: String): String {
        val inputStream = ClassLoader.getSystemResourceAsStream(filename)
        return String(ByteStreams.toByteArray(inputStream))
    }

    @Test
    fun getLanguagesShouldFetchLanguagesOfExam() {
        val languagesJson = getContentFromFile("languages.json")
        val successResponse = MockResponse().setResponseCode(200).setBody(languagesJson)
        mockWebServer.enqueue(successResponse)

        runBlocking {
            val response = service.getLanguages("slug").execute()
            mockWebServer.takeRequest()
            val languages = response.body().results

            Assert.assertTrue(response.isSuccessful)
            Assert.assertEquals(2, languages.size)
            Assert.assertEquals("hi", languages[0].code)
            Assert.assertEquals("en", languages[1].code)
        }
    }

}