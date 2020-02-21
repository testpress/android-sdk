package `in`.testpress.course.repository

import `in`.testpress.core.TestpressException
import `in`.testpress.course.enums.DataSource
import `in`.testpress.course.models.Resource
import `in`.testpress.course.util.RetrofitCallMock
import `in`.testpress.course.util.mock
import `in`.testpress.network.RetrofitCall
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


class NetworkBoundResourceTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var networkBoundResource: NetworkBoundResource<Foo, Foo>
    private lateinit var handleSaveCallResult: (Foo) -> Unit
    private lateinit var handleShouldMatch: (Foo?) -> Boolean
    private lateinit var handleCreateCall: () -> RetrofitCall<Foo>
    private val fetchedOnce = AtomicBoolean(false)
    private val dbData = MutableLiveData<Foo>()
    val exception = TestpressException.networkError(IOException())

    @Before
    fun setUp() {
        networkBoundResource = object : NetworkBoundResource<Foo, Foo>() {
            override fun saveCallResult(item: Foo) {
                handleSaveCallResult(item)
            }

            override fun shouldFetch(data: Foo?): Boolean {
                return handleShouldMatch(data) && fetchedOnce.compareAndSet(false, true)
            }

            override fun loadFromDb(): LiveData<Foo> {
                return dbData
            }

            override fun createCall(): RetrofitCall<Foo> {
                return handleCreateCall()
            }
        }
    }

    private fun assertInitialValue(observer: Observer<Resource<Foo>>) {
        networkBoundResource.asLiveData().observeForever(observer)
        verify(observer).onChanged(Resource.loading(null))
        reset(observer)
    }

    @Test
    fun basicFromNetwork() {
        val saved = AtomicReference<Foo>()
        handleShouldMatch = { it == null }
        val fetchedDbValue = Foo(1)
        handleSaveCallResult = { foo ->
            saved.set(foo)
            dbData.setValue(fetchedDbValue)
        }
        handleCreateCall = { RetrofitCallMock(Resource.success(Foo(1))) }
        val observer = mock<Observer<Resource<Foo>>>()
        assertInitialValue(observer)
        dbData.value = null

        assertThat(saved.get(), `is`(Foo(1)))
        verify(observer).onChanged(Resource.success(fetchedDbValue))
    }

    @Test
    fun failureFromNetwork() {
        val saved = AtomicBoolean(false)
        handleShouldMatch = { it == null }
        handleSaveCallResult = {
            saved.set(true)
        }
        handleCreateCall = { RetrofitCallMock(Resource.error(exception, null)) }
        val observer = mock<Observer<Resource<Foo>>>()
        assertInitialValue(observer)
        dbData.value = null

        assertThat(saved.get(), `is`(false))
        verify(observer).onChanged(Resource.error(exception, null))
        verifyNoMoreInteractions(observer)

    }

    @Test
    fun dbSuccessWithoutNetwork() {
        var saved = false
        handleShouldMatch = { it == null }
        handleSaveCallResult = {
            saved = true
        }
        val observer = mock<Observer<Resource<Foo>>>()
        assertInitialValue(observer)

        val dbFoo = Foo(1)
        dbData.value = dbFoo
        verify(observer).onChanged(Resource.success(dbFoo, DataSource.DB))
        assertThat(saved, `is`(false))
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun dbSuccessWithFetchFailure() {
        val dbValue = Foo(1)
        var saved = false
        handleShouldMatch = { foo -> foo === dbValue }
        handleSaveCallResult = {
            saved = true
        }
        val apiCall = RetrofitCallMock<Foo>(Resource.error(exception, null))
        handleCreateCall = { apiCall }
        val observer = mock<Observer<Resource<Foo>>>()
        assertInitialValue(observer)

        dbData.value = dbValue
        verify(observer).onChanged(Resource.loading(dbValue))
        assertThat(saved, `is`(false))
        verify(observer).onChanged(Resource.error(exception, dbValue))
        verifyNoMoreInteractions(observer)
    }

    @Test
    fun dbSuccessWithReFetchSuccess() {
        val dbValue = Foo(1)
        val dbValue2 = Foo(2)
        val saved = AtomicReference<Foo>()
        handleShouldMatch = { foo -> foo === dbValue }
        handleSaveCallResult = { foo ->
            saved.set(foo)
            dbData.setValue(dbValue2)
        }
        val networkResult = Foo(1)
        val apiCall = RetrofitCallMock(Resource.success(networkResult))
        handleCreateCall = { apiCall }

        val observer = mock<Observer<Resource<Foo>>>()
        assertInitialValue(observer)

        dbData.value = dbValue
        verify(observer).onChanged(Resource.loading(dbValue))
        assertThat(saved.get(), `is`(networkResult))
        verify(observer).onChanged(Resource.success(dbValue2))
        verifyNoMoreInteractions(observer)
    }

    private data class Foo(var value: Int)


}