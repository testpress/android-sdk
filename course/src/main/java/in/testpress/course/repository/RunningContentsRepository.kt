package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asListOfDomainContents
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.network.Resource
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RunningContentsRepository(val context: Context, val courseId: Long = -1) {
    private val runningContentDao = TestpressDatabase.invoke(context).runningContentDao()
    val courseNetwork = CourseNetwork(context)
    var page = 1

    private var _resourceContents: MutableLiveData<Resource<List<DomainContent>>> = MutableLiveData()
    val resourceContents: LiveData<Resource<List<DomainContent>>>
        get() = _resourceContents

    fun loadItems(page: Int = 1) {
        val queryParams = hashMapOf<String, Any>("page" to page)
        courseNetwork.getRunningContents(courseId, queryParams)
            .enqueue(object : TestpressCallback<ApiResponse<List<RunningContentEntity>>>(){
                override fun onException(exception: TestpressException?) {
                    val contents = getAll()
                    if (contents.value?.isNotEmpty() == true) {
                        _resourceContents.postValue(Resource.error(exception!!, contents.value?.asListOfDomainContents()))
                    } else {
                        _resourceContents.postValue(Resource.error(exception!!, null))
                    }
                }

                override fun onSuccess(result: ApiResponse<List<RunningContentEntity>>) {
                    handleFetchSuccess(result)
                }
            })
    }

    private fun handleFetchSuccess(response: ApiResponse<List<RunningContentEntity>>) {
        CoroutineScope(Dispatchers.IO).launch {
            storeContent(response.results)
            _resourceContents.postValue(Resource.success(response.results.asListOfDomainContents()))
            if (response.next != null) {
                page += 1
                loadItems(page)
            } else {
                page = 1
            }
        }
    }

    private fun getAll(): LiveData<List<RunningContentEntity>> {
        return runningContentDao.getAll()
    }

    private suspend fun storeContent(response: List<RunningContentEntity>): List<DomainContent> {
        runningContentDao.insertAll(response)
        return response.asListOfDomainContents()
    }
}