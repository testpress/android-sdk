package `in`.testpress.course.repository

import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.Resource
import `in`.testpress.network.RetrofitCall
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            data?.let { setValue(Resource.success(data)) }
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
            val response = createCall().execute()
            if (response.isSuccessful) {
                saveNetworkResponseToDB(processNetworkResponse(response.body()))
                refreshDBSource()
                showDBDataIfAvailable()
            } else {
                onFetchFailed()
                result.addSource(dbSource) { newData ->
                    val exception = TestpressException.httpError(response)
                    setValue(Resource.error(exception, null))
                }
            }
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