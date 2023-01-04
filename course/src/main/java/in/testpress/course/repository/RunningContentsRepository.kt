package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asDomainContents
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.network.Resource
import `in`.testpress.v2_4.models.ApiResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class RunningContentsRepository(val context: Context, val courseId: Long = -1) {
    val contentDao = TestpressSDKDatabase.getContentDao(context)
    val courseNetwork = CourseNetwork(context)

    private var _resourceContents: MutableLiveData<Resource<List<DomainContent>>> = MutableLiveData()
    val resourceContents: LiveData<Resource<List<DomainContent>>>
        get() = _resourceContents

    init {
        val contents = contentDao.queryBuilder()
            .where(ContentDao.Properties.CourseId.eq(courseId))
            .list()
        if (contents.isNotEmpty()) {
            val sortedContents = sortContentsByOrder(contents)
            _resourceContents.postValue(Resource.success(sortedContents.asDomainContents()))
        } else {
            _resourceContents.value = Resource.loading(null)
        }
    }

    fun loadItems() {
        courseNetwork.getRunningContents(courseId)
            .enqueue(object : TestpressCallback<ApiResponse<List<Content>>>(){
                override fun onException(exception: TestpressException?) {
                    val contents = getAll()
                    if (contents?.isNotEmpty() == true) {
                        _resourceContents.postValue(Resource.error(exception!!, contents.asDomainContents()))
                    } else {
                        _resourceContents.postValue(Resource.error(exception!!, null))
                    }
                }

                override fun onSuccess(result: ApiResponse<List<Content>>) {
                    handleFetchSuccess(result)
                }
            })
    }

    private fun handleFetchSuccess(response: ApiResponse<List<Content>>) {
        deleteExistingContents()
        storeContent(response.results)
        val contents = contentDao.queryBuilder()
            .where(ContentDao.Properties.CourseId.eq(courseId))
            .list()
        val sortedContents = sortContentsByOrder(contents)
        _resourceContents.postValue(Resource.success(sortedContents.asDomainContents()))
    }

    private fun deleteExistingContents() {
        contentDao.queryBuilder()
            .where(ContentDao.Properties.CourseId.eq(courseId))
            .buildDelete()
            .executeDeleteWithoutDetachingEntities()
    }

    private fun getAll(): MutableList<Content>? {
        return contentDao.queryBuilder()
            .where(ContentDao.Properties.CourseId.eq(courseId))
            .list()
    }

    private fun storeContent(response: List<Content>): List<DomainContent> {

        val contentDao = TestpressSDKDatabase.getContentDao(context)
        contentDao.insertOrReplaceInTx(response)

        return response.asDomainContents()
    }

    private fun sortContentsByOrder(contents: List<Content>): List<Content> {
        return contents.sortedWith(compareBy {
            it.id
        })
    }
}