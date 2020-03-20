package `in`.testpress.course.repository

import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.Resource
import `in`.testpress.course.util.RetrofitCallMock
import `in`.testpress.course.util.mock
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.RetrofitCall
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import java.io.IOException

class MultiPageNetworkBoundResourceTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var networkBoundResource: MultiPageNetworkBoundResource<Foo, Foo>
    private lateinit var saveNetworkResponseToDBHandler: (List<Foo>) -> Unit
    private lateinit var shouldFetchHandler: (Foo?) -> Boolean
    private lateinit var createCallHandler: () -> RetrofitCall<TestpressApiResponse<Foo>>
    private lateinit var clearDBHandler: () -> Unit

    private val dbData = MutableLiveData<Foo>()
    private lateinit var observer: Observer<Resource<Foo>>
    private val exception = TestpressException.networkError(IOException())
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)

        runBlocking {
            networkBoundResource = object : MultiPageNetworkBoundResource<Foo, Foo>() {
                override fun saveNetworkResponseToDB(item: List<Foo>) {
                    saveNetworkResponseToDBHandler(item)
                }

                override fun shouldFetch(data: Foo?): Boolean {
                    return shouldFetchHandler(data)
                }

                override fun loadFromDb(): LiveData<Foo> {
                    return dbData
                }

                override fun createCall(url: String?): RetrofitCall<TestpressApiResponse<Foo>> {
                    return createCallHandler()
                }

                override fun shouldClearDB() = true

                override fun clearFromDB() {
                    return clearDBHandler()
                }
            }
        }
    }

    private fun initObserver() {
        observer = mock<Observer<Resource<Foo>>>()
        networkBoundResource.asLiveData().observeForever(observer)
        Mockito.reset(observer)
    }

    @Test
    fun doesNextPageFetchCallIsMadeIfApiHasNextPage() = runBlocking {
        var networkCallCount = 0
        shouldFetchHandler = { it == null }
        val apiResponse = TestpressApiResponse<Foo>()
        apiResponse.results = listOf(Foo(1))
        apiResponse.next = "next_page_url"
        createCallHandler = {
            networkCallCount += 1
            if (networkCallCount == 2) {
                apiResponse.next = null
            }
            RetrofitCallMock(Resource.success(apiResponse))
        }

        initObserver()
        dbData.value = null
        delay(100)

        Assert.assertTrue(2 == networkCallCount)
    }

    @Test
    fun clearDBShouldBeCalledIfShouldClearDBIsTrue() = runBlocking{
        var isClearDBCalled = false
        shouldFetchHandler = {true}
        val apiResponse = TestpressApiResponse<Foo>()
        createCallHandler = {
            RetrofitCallMock(Resource.success(apiResponse))
        }
        clearDBHandler = {isClearDBCalled = true}

        initObserver()
        dbData.value = null
        delay(100)

        Assert.assertTrue(isClearDBCalled)
    }

    data class Foo(var value: Int)
}