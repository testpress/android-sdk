package `in`.testpress.course.repository

import `in`.testpress.core.TestpressException
import `in`.testpress.network.Resource
import `in`.testpress.network.RetrofitCall
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class NetworkBoundResource<ResultDataType, NetworkDataType> {
    private val result = MediatorLiveData<Resource<ResultDataType>>()
    private var dbSource = loadFromDb()

    init {
        result.value = Resource.loading(null)
        showDBDataIfAvailable(true) { updateDBDataIfNeeded() }
    }


    private fun showDBDataIfAvailable(nonce: Boolean = false, callback: ()->Unit = {}) {
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

    private fun updateDBDataIfNeeded() {
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
        println("Handle Response : ${response.isSuccessful}")
        if (response.isSuccessful) {
            saveNetworkResponseToDB(processNetworkResponse(response.body()))
            refreshDBSource()
            withContext(Dispatchers.Main) {
                println("NBT : ")
                showDBDataIfAvailable()
            }
        } else {
            onFetchFailed()
            result.addSource(dbSource) { newData ->
                val exception = TestpressException.httpError(response)
                setValue(Resource.error(exception, null))
            }
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