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

abstract class BaseDetailRepository<NetworkResponseT, DomainEntityT>(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    protected val database = TestpressDatabase.invoke(context.applicationContext)

    protected val _resource = MutableLiveData<Resource<DomainEntityT>>()
    val resource: LiveData<Resource<DomainEntityT>> get() = _resource

    fun loadFromDatabase() {
        _resource.value = Resource.loading(null)

        scope.launch {
            val cached = getFromDb()
            if (cached != null) {
                withContext(Dispatchers.Main) {
                    _resource.value = Resource.success(cached)
                }
            }
            fetchFromNetwork()
        }
    }

    fun retry() {
        fetchFromNetwork()
    }

    fun cancelScope() {
        scope.cancel()
    }

    private fun fetchFromNetwork() {
        _resource.postValue(Resource.loading(_resource.value?.data))

        makeNetworkCall(object : TestpressCallback<Any>() {
            override fun onSuccess(result: Any) {
                scope.launch {
                    saveToDb(result as NetworkResponseT)
                    updateLiveDataFromDb()
                }
            }

            override fun onException(exception: TestpressException) {
                _resource.postValue(Resource.error(exception, _resource.value?.data))
            }
        })
    }

    protected abstract suspend fun getFromDb(): DomainEntityT?
    protected abstract suspend fun saveToDb(response: NetworkResponseT)
    protected abstract suspend fun updateLiveDataFromDb()
    protected abstract fun makeNetworkCall(callback: TestpressCallback<*>)
}