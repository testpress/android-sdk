package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.models.Resource
import `in`.testpress.network.RetrofitCall
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread

abstract class NetworkBoundResource<ResultType, RequestType> {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        val dbSource = loadFromDb()
        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            if (shouldFetch(data)) {
                setValue(Resource.loading(data))
                fetchFromNetwork(dbSource)
            } else {
                result.addSource(dbSource) { newData ->
                    newData?.let {
                        setValue(Resource.success(newData))
                    }
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        createCall().enqueue(object : TestpressCallback<RequestType>() {
            override fun onSuccess(response: RequestType) {
                val liveData = MutableLiveData<RequestType>()
                liveData.value = response
                result.addSource(liveData) {
                    result.removeSource(liveData)
                    result.removeSource(dbSource)
                    saveCallResult(processResponse(response))
                    result.addSource(loadFromDb()) { newData ->
                        setValue(Resource.success(newData))
                    }
                }
            }

            override fun onException(exception: TestpressException) {
                onFetchFailed()
                result.addSource(dbSource) { newData ->
                    setValue(Resource.error(exception, newData))
                }
            }
        })
    }

    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): RetrofitCall<RequestType>

    @WorkerThread
    protected open fun processResponse(response: RequestType) = response

    protected open fun onFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultType>>
}