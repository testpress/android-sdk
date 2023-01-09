package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asListOfDomainContents
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.database.entities.RunningContentEntity
import `in`.testpress.network.Resource
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class RunningContentsRepository(val context: Context, val courseId: Long = -1) {
    val courseNetwork = CourseNetwork(context)
    var page = 1
    private var dataList = mutableListOf<RunningContentEntity>()

    private var _resourceContents: MutableLiveData<Resource<List<DomainContent>>> = MutableLiveData()
    val resourceContents: LiveData<Resource<List<DomainContent>>>
        get() = _resourceContents

    init {
        _resourceContents.postValue(Resource.loading(null))
    }

    fun loadItems(page: Int = 1) {
        val queryParams = hashMapOf<String, Any>("page" to page)
        courseNetwork.getRunningContents(courseId, queryParams)
            .enqueue(object : TestpressCallback<ApiResponse<List<RunningContentEntity>>>(){
                override fun onException(exception: TestpressException?) {
                    _resourceContents.postValue(Resource.error(exception!!, null))
                }

                override fun onSuccess(result: ApiResponse<List<RunningContentEntity>>) {
                    handleFetchSuccess(result)
                }
            })
    }

    private fun handleFetchSuccess(response: ApiResponse<List<RunningContentEntity>>) {
        storeContent(response.results)
        val contents = getAll()
        if (contents.isNotEmpty()) {
            _resourceContents.postValue(Resource.success(contents.asListOfDomainContents()))
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

    private fun getAll(): List<RunningContentEntity> {
        return dataList
    }

    private fun storeContent(response: List<RunningContentEntity>): List<DomainContent> {
        if (page == 1){
            dataList.removeAll(dataList)
        }
        for (data in response){
            if (!dataList.contains(data)){
                dataList.add(data)
            }
        }
        return dataList.asListOfDomainContents()
    }

//    private fun sort() :List<RunningContentEntity> {
//
//        val content = getAll()
//
//        Log.d("TAG", "sort: ${content[0].start}")
//
//        val dateTimeFormatter: DateTimeFormatter =
//            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
//
//        val result = content.sortedByDescending {
//            LocalDate.parse(it.start, dateTimeFormatter)
//        }
//        println(result)
//        return result
//    }
}