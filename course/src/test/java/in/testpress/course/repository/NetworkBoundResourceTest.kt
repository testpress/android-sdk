package `in`.testpress.course.repository

import `in`.testpress.core.TestpressException
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.Resource
import `in`.testpress.course.util.RetrofitCallMock
import `in`.testpress.course.util.getOrAwaitValue
import `in`.testpress.course.util.mock
import `in`.testpress.network.RetrofitCall
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import java.io.IOException

class NetworkBoundResourceTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var networkBoundResource: NetworkBoundResource<Foo, Foo>
    private lateinit var saveNetworkResponseToDBHandler: (Foo) -> Unit
    private lateinit var shouldFetchHandler: (Foo?) -> Boolean
    private lateinit var createCallHandler: () -> RetrofitCall<Foo>

    private val dbData = MutableLiveData<Foo>()
    private lateinit var observer: Observer<Resource<Foo>>
    private val exception = TestpressException.networkError(IOException())
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)

        runBlocking {
            networkBoundResource = object : NetworkBoundResource<Foo, Foo>() {
                override fun saveNetworkResponseToDB(item: Foo) {
                    saveNetworkResponseToDBHandler(item)
                }

                override fun shouldFetch(data: Foo?): Boolean {
                    return shouldFetchHandler(data)
                }

                override fun loadFromDb(): LiveData<Foo> {
                    return dbData
                }

                override fun createCall(): RetrofitCall<Foo> {
                    return createCallHandler()
                }
            }
        }
    }

    private fun initObserver() {
        observer = mock<Observer<Resource<Foo>>>()
        networkBoundResource.asLiveData().observeForever(observer)
        reset(observer)
    }

    @Test
    fun doesDbDataDisplayedIfNotEmpty() = runBlocking {
        shouldFetchHandler = { false }
        initObserver()
        val dbValue = Foo(1)
        dbData.value = dbValue
        delay(50)

        verify(observer).onChanged(Resource.success(dbValue))
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun doesNetworkRequestedIfDbEmpty() = runBlocking {
        var isNetworkCallMade = false
        shouldFetchHandler = { it == null }
        createCallHandler = {
            isNetworkCallMade = true
            RetrofitCallMock(Resource.success(Foo(1)))
        }

        initObserver()
        dbData.value = null
        delay(50)

        Assert.assertTrue(isNetworkCallMade)
    }

    @Test
    fun ifErrorDispatchedOnNetworkError() = runBlocking {
        shouldFetchHandler = { true }
        createCallHandler = { RetrofitCallMock(Resource.error(exception, null)) }
        initObserver()
        dbData.value = null
        delay(500)

        val resource = networkBoundResource.asLiveData().getOrAwaitValue()
        Assert.assertEquals(resource.status, Status.ERROR)
    }

    @Test
    fun doesDataSavedToDiskOnNetworkSucess() = runBlocking {
        var saved = false
        shouldFetchHandler = { true }
        saveNetworkResponseToDBHandler = { saved = true }
        createCallHandler = { RetrofitCallMock(Resource.success(Foo(1))) }
        initObserver()
        dbData.value = null
        delay(50)

        Assert.assertTrue(saved)
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun doesNetworkRequestedIfDataInvalidated() = runBlocking {
        var isNetworkCallMade = false
        shouldFetchHandler = { true }
        createCallHandler = {
            isNetworkCallMade = true
            RetrofitCallMock(Resource.success(Foo(1)))
        }
        initObserver()
        dbData.value = null
        delay(50)

        Assert.assertTrue(isNetworkCallMade)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun onNetworkErrorExceptionIsSetOnResource() = runBlocking {
        val exception = Exception()
        shouldFetchHandler = { true }
        createCallHandler = {
            throw exception
        }
        initObserver()
        dbData.value = null
        delay(50)
        val resource = networkBoundResource.asLiveData().getOrAwaitValue()

        Assert.assertEquals(Status.ERROR, resource.status)
    }

    data class Foo(var value: Int)
}