package `in`.testpress.network

import `in`.testpress.core.TestpressException
import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class NetworkBoundResource<ResultDataType, NetworkDataType> {
    private val result = MutableLiveData<Resource<ResultDataType>>()
    private var dbSource: LiveData<ResultDataType>

    init {
        result.value = Resource.loading(null)
        dbSource = loadFromDb()
        dbSource.observeOnce {
            if (shouldFetch(it)) {
                result.postValue(Resource.loading(it))
                GlobalScope.launch {
                    fetchFromNetwork()
                }
            } else {
                result.postValue(Resource.success(it))
            }
        }
    }

    private fun loadFreshData() {
        dbSource.observeOnce {
            result.postValue(Resource.success(it))
        }
    }

    private fun refreshDBSource() {
        dbSource = loadFromDb()
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultDataType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    private suspend fun fetchFromNetwork() {
        withContext(Dispatchers.IO) {
            try {
                val response = createCall().execute()
                handleResponse(response)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private suspend fun handleResponse(response: Response<NetworkDataType>) {
        if (response.isSuccessful) {
            saveNetworkResponseToDB(processNetworkResponse(response.body()))
            refreshDBSource()
            withContext(Dispatchers.Main) {
                loadFreshData()
            }
        } else {
            onFetchFailed()
            val exception = TestpressException.httpError(response)
            setValue(Resource.error(exception, null))
        }
    }

    private suspend fun handleException(exception: Exception) {
        withContext(Dispatchers.Main) {
            setValue(Resource.error(TestpressException.unexpectedError(exception), null))
        }
    }

    protected abstract fun saveNetworkResponseToDB(item: NetworkDataType)

    protected abstract fun shouldFetch(data: ResultDataType?): Boolean

    protected abstract fun loadFromDb(): LiveData<ResultDataType>

    protected abstract fun createCall(): RetrofitCall<NetworkDataType>

    protected open fun processNetworkResponse(response: NetworkDataType) = response

    protected open fun onFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultDataType>>
}

// https://stackoverflow.com/a/56479746/400236
fun <T> LiveData<T>.observeOnce(callback: (T) -> Unit) {
    observeForever(object: Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            callback(value)
        }
    })
}