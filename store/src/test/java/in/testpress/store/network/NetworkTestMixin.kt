package `in`.testpress.store.network

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

    lateinit var service: ProductService

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addCallAdapterFactory(ErrorHandlingCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ProductService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    fun getProductListFromFile(filename: String): String {
        val inputStream = ClassLoader.getSystemResourceAsStream(filename)
        return String(ByteStreams.toByteArray(inputStream))
    }
}
