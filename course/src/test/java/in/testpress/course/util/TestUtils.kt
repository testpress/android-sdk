package `in`.testpress.course.util

import `in`.testpress.core.TestpressCallback
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.Resource
import `in`.testpress.network.RetrofitCall
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.mockito.Mockito
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class RetrofitCallMock<T>(val resource: Resource<T>) : RetrofitCall<T> {
    override fun execute(): Response<T> {
        val responseBody =
            ResponseBody.create(MediaType.parse("application/json"), """{"detail":"Error"}""")
        return when (resource.status) {
            Status.SUCCESS -> Response.success(resource.data)
            Status.ERROR -> {
                Response.error(
                    responseBody, okhttp3.Response.Builder()
                        .code(500)
                        .message("Error")
                        .protocol(Protocol.HTTP_1_1)
                        .request(Request.Builder().url("http://localhost/").build())
                        .build()
                )
            }
            else -> Response.success(resource.data)
        }
    }

    override fun clone(): RetrofitCall<T> {
        return this
    }

    override fun cancel() {
    }

    override fun enqueue(callback: TestpressCallback<T>?): RetrofitCall<T> {
        when (resource.status) {
            Status.SUCCESS -> callback?.onSuccess(resource.data)
            Status.ERROR -> callback?.onException(resource.exception)
        }
        return this
    }
}

inline fun <reified T> mock(): T = Mockito.mock(T::class.java)

fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)

    afterObserve.invoke()

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}