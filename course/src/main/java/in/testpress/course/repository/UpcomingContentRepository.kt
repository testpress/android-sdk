package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.convertUpcomingContentsToDomainContents
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.database.entities.UpcomingContentEntity
import `in`.testpress.network.Resource
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UpcomingContentRepository(val context: Context, val courseId: Long = -1) {
    private val upcomingContentDao = TestpressDatabase.invoke(context).upcomingContentDao()
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
        courseNetwork.getUpcomingContents(courseId, queryParams)
            .enqueue(object : TestpressCallback<ApiResponse<List<UpcomingContentEntity>>>() {
                override fun onException(exception: TestpressException?) {
                    val contents = getAll()
                    if (contents.isNotEmpty()) {
                        _resourceContents.postValue(Resource.error(exception!!, contents))
                    } else {
                        _resourceContents.postValue(Resource.error(exception!!, null))
                    }
                }

                override fun onSuccess(result: ApiResponse<List<UpcomingContentEntity>>) {
                    handleFetchSuccess(result)
                }
            })
    }

    private fun handleFetchSuccess(response: ApiResponse<List<UpcomingContentEntity>>) {
        CoroutineScope(Dispatchers.IO).launch {
            storeContent(response.results)
            val contents = getAll()
            if (contents.isNotEmpty()) {
                _resourceContents.postValue(Resource.success(contents))
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
        return upcomingContentDao.getAll(courseId).convertUpcomingContentsToDomainContents()
    }

    private suspend fun storeContent(response: List<UpcomingContentEntity>): List<UpcomingContentEntity> {
        if (page == 1) {
            upcomingContentDao.deleteAll(courseId)
        }
        upcomingContentDao.insertAll(response)
        return response
    }

    private fun sort(): List<DomainContent> {
        val content = getAll()
        val dateTimeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
        val result = content.sortedByDescending {
            LocalDate.parse(it.start, dateTimeFormatter)
        }
        return result
    }
}