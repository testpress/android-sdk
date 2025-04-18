package `in`.testpress.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.network.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BasePaginatedRepository<NetworkResponseT, DomainEntityT>(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    protected val database = TestpressDatabase.invoke(context.applicationContext)
    private var currentPage = 1
    private var isLoading = false
    private var hasNextPage = true
    private var lastFailedPage: Int? = null

    protected val _resource = MutableLiveData<Resource<List<DomainEntityT>>>()
    val resource: LiveData<Resource<List<DomainEntityT>>> get() = _resource

    protected fun loadFromDatabase() {
        _resource.value = Resource.loading(_resource.value?.data)
        scope.launch {
            val cached = getFromDb()
            if (cached.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    _resource.value = Resource.success(cached)
                }
            }
            fetchFromNetwork()
        }
    }

    fun fetchNextPage() {
        if (isLoading || !hasNextPage || lastFailedPage == currentPage) return
        currentPage++
        fetchFromNetwork()
    }

    fun retryNextPage() {
        fetchFromNetwork()
    }

    fun resetPaginationAndFetch() {
        currentPage = 1
        isLoading = false
        hasNextPage = true
        lastFailedPage = null
        loadFromDatabase()
    }

    fun cancelScope() {
        scope.cancel()
    }

    private fun fetchFromNetwork() {
        if (isLoading || !hasNextPage) return
        isLoading = true

        _resource.postValue(Resource.loading(_resource.value?.data))

        val queryParams = hashMapOf<String, Any>("page" to currentPage)
        makeNetworkCall(queryParams, object : TestpressCallback<Any>() {
            override fun onSuccess(result: Any) {
                isLoading = false
                lastFailedPage = null
                hasNextPage = extractNextPageAvailable(result)
                scope.launch { handleSuccess(result as NetworkResponseT) }
            }

            override fun onException(exception: TestpressException) {
                isLoading = false
                lastFailedPage = currentPage
                _resource.postValue(Resource.error(exception, _resource.value?.data))
            }
        })
    }

    private suspend fun handleSuccess(response: NetworkResponseT) {
        if (currentPage == 1) clearLocalDb()
        saveToDb(response)
        updateLiveDataFromDb()
    }

    protected abstract suspend fun getFromDb(): List<DomainEntityT>
    protected abstract suspend fun clearLocalDb()
    protected abstract suspend fun saveToDb(response: NetworkResponseT)
    protected abstract suspend fun updateLiveDataFromDb()
    protected abstract fun makeNetworkCall(
        queryParams: Map<String, Any>,
        callback: TestpressCallback<*>
    )

    protected abstract fun extractNextPageAvailable(response: Any): Boolean
}