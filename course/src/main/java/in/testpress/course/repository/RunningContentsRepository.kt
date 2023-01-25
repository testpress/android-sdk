package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.convertRunningContentsToDomainContents
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.network.Resource
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class RunningContentsRepository(val context: Context, val courseId: Long = -1) {
    private val runningContentDao = TestpressDatabase.invoke(context).runningContentDao()
    val courseNetwork = CourseNetwork(context)
    var page = 1
    private var _resourceContents: MutableLiveData<Resource<List<DomainContent>>> =
        MutableLiveData()
    val resourceContents: LiveData<Resource<List<DomainContent>>>
        get() = _resourceContents

    init {
        _resourceContents.postValue(Resource.loading(null))
    }

    fun loadItems(page: Int = 1) {
        val queryParams = hashMapOf<String, Any>("page" to page)
        courseNetwork.getRunningContents(courseId, queryParams)
            .enqueue(object : TestpressCallback<ApiResponse<List<RunningContentEntity>>>() {
                override fun onException(exception: TestpressException?) {
                    val contents = sort(getAll())
                    if (contents.isNotEmpty()) {
                        _resourceContents.postValue(Resource.error(exception!!, contents))
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
            val contents = sort(getAll())
            if (contents.isNotEmpty()) {
                _resourceContents.postValue(Resource.success(contents))
            } else {
                _resourceContents.postValue(Resource.success(listOf()))
            }
            if (response.next != null) {
                page += 1
                loadItems(page)
            } else {
                page = 1
            }

        }
    }

    private fun getAll(): List<DomainContent> {
        return runBlocking(Dispatchers.IO) {
            runningContentDao.getAll(courseId).convertRunningContentsToDomainContents()
        }
    }

    private suspend fun storeContent(response: List<RunningContentEntity>): List<RunningContentEntity> {
        if (page == 1) {
            runningContentDao.deleteAll(courseId)
        }
        runningContentDao.insertAll(response)
        return response
    }

    private fun sort(contents: List<DomainContent>): List<DomainContent> {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        val result = contents.sortedBy { domainContent ->
            domainContent.end?.let { simpleDateFormat.parse(it) }?.time
        }
        return result.sortedBy { it.end == null }
    }
}