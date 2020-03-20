package `in`.testpress.course.repository

import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.Resource
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseNetworkBoundResource<ResultDataType, NetworkDataType> {
    protected val result = MediatorLiveData<Resource<ResultDataType>>()
    protected var dbSource = loadFromDb()

    init {
        result.value = Resource.loading(null)
        showDBDataIfAvailable(true) { updateDBDataIfNeeded() }
    }

    protected fun showDBDataIfAvailable(nonce: Boolean = false, callback: ()->Unit = {}) {
        result.addSource(dbSource) { data ->
            if (nonce) result.removeSource(dbSource)
            if (nonce && shouldFetch(data)) {
                data?.let { setValue(Resource.loading(data)) }
            } else {
                data?.let { setValue(Resource.success(data)) }
            }
            callback()
        }
    }

    protected fun updateDBDataIfNeeded() {
        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            if (shouldFetch(data)) {
                setValue(Resource.loading(data))
                GlobalScope.launch {
                    fetchFromNetwork()
                }
            }
        }
    }

    protected fun reloadFromDB() {
        dbSource = loadFromDb()
    }

    @MainThread
    protected fun setValue(newValue: Resource<ResultDataType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }


    protected suspend fun handleException(exception: Exception) {
        withContext(Dispatchers.Main) {
            setValue(Resource.error(TestpressException.unexpectedError(exception), null))
        }
    }

    protected abstract fun loadFromDb(): LiveData<ResultDataType>

    protected abstract fun shouldFetch(data: ResultDataType?): Boolean

    protected open suspend fun fetchFromNetwork() {}

    protected open fun onFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultDataType>>
}