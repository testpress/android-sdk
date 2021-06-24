package `in`.testpress.network

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

    lateinit var service: TestpressAPIService

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addCallAdapterFactory(ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TestpressAPIService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }


}