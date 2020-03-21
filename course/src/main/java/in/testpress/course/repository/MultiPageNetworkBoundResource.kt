package `in`.testpress.course.repository

import `in`.testpress.core.TestpressException
import `in`.testpress.course.network.Resource
import `in`.testpress.models.TestpressApiResponse
import `in`.testpress.network.RetrofitCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class MultiPageNetworkBoundResource<ResultDataType, NetworkDataType> :
    BaseNetworkBoundResource<ResultDataType, NetworkDataType>() {
    private val apiResults = mutableListOf<NetworkDataType>()


    override suspend fun fetchFromNetwork() {
        withContext(Dispatchers.IO) {
            try {
                makeNetworkCall(null)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private suspend fun makeNetworkCall(url: String?) {
        val response = createCall(url).execute()
        handleApiResponse(response)
    }

    private suspend fun handleApiResponse(response: Response<TestpressApiResponse<NetworkDataType>>) {
        if (response.isSuccessful) {
            val apiResponse = response.body()
            apiResults.addAll(apiResponse.results)

            if (apiResponse.hasMore()) {
                makeNetworkCall(apiResponse.next)
            } else {
                if (shouldClearDB()) clearFromDB()
                saveNetworkResponseToDB(apiResults)
                reloadFromDB()
                withContext(Dispatchers.Main) {
                    showDBDataIfAvailable()
                }
            }
        } else {
            onFetchFailed()
            result.addSource(dbSource) { newData ->
                val exception = TestpressException.httpError(response)
                exception.printStackTrace()
                setValue(Resource.error(exception, null))
            }
        }
    }


    protected abstract fun saveNetworkResponseToDB(item: List<NetworkDataType>)

    protected abstract fun createCall(url: String? = null): RetrofitCall<TestpressApiResponse<NetworkDataType>>

    protected open fun shouldClearDB(): Boolean = false

    protected open fun clearFromDB() {}
}