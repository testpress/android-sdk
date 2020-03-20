package `in`.testpress.course.repository

import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.Resource
import `in`.testpress.network.RetrofitCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class NetworkBoundResource<ResultDataType, NetworkDataType>:
    BaseNetworkBoundResource<ResultDataType, NetworkDataType>() {

    override suspend fun fetchFromNetwork() {
        withContext(Dispatchers.IO) {
            try {
                val response = createCall().execute()
                handleResponse(response)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    protected suspend fun handleResponse(response: Response<NetworkDataType>) {
        if (response.isSuccessful) {
            saveNetworkResponseToDB(processNetworkResponse(response.body()))
            reloadFromDB()
            withContext(Dispatchers.Main) {
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

    protected abstract fun saveNetworkResponseToDB(item: NetworkDataType)

    protected abstract fun createCall(): RetrofitCall<NetworkDataType>

    protected open fun processNetworkResponse(response: NetworkDataType) = response
}