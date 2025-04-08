package `in`.testpress.exam.network

import `in`.testpress.exam.network.service.CommentService
import `in`.testpress.network.ErrorHandlingCallAdapterFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.io.ByteStreams
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class NetworkTestMixin {

    lateinit var mockWebServer: MockWebServer

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var service: CommentService

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addCallAdapterFactory(ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CommentService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    fun getResponseFromFile(filename: String): String {
        val inputStream = ClassLoader.getSystemResourceAsStream(filename)
        return String(ByteStreams.toByteArray(inputStream))
    }
}
