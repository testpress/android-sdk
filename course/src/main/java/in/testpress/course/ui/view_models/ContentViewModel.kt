package `in`.testpress.course.ui.view_models

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.enums.Status
import `in`.testpress.course.models.Resource
import `in`.testpress.course.network.TestpressCourseApiClient
import `in`.testpress.course.network.TestpressCourseApiClient.CONTENTS_PATH_v2_4
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.models.greendao.CourseAttempt
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData


class ContentViewModel(application: Application) : AndroidViewModel(application) {
    var content: MutableLiveData<Content> = MutableLiveData()
    private var resourceContent: MutableLiveData<Resource<Content>> = MutableLiveData()
    private var contentAttempt: MutableLiveData<Resource<CourseAttempt>> = MutableLiveData()
    private val contentDao: ContentDao = TestpressSDKDatabase.getContentDao(getApplication())
    private val courseApiClient: TestpressCourseApiClient

    init {
        courseApiClient = TestpressCourseApiClient(getApplication())
    }

    fun loadContent(id: Int): LiveData<Resource<Content>> {
        val url = "${CONTENTS_PATH_v2_4}${id}/"
        courseApiClient.getContent(url).enqueue(object : TestpressCallback<Content>() {
            override fun onSuccess(result: Content?) {
                content.value = result
                resourceContent.value = Resource(Status.SUCCESS, result, null)
                contentDao.insertOrReplaceInTx(result)
            }

            override fun onException(exception: TestpressException?) {
                resourceContent.value = Resource(Status.ERROR, null, exception)
            }
        })

        return resourceContent
    }

    fun getChapterContents(chapterId: Long): List<Content> {
        return contentDao.queryBuilder().where(
                ContentDao.Properties.ChapterId.eq(chapterId),
                ContentDao.Properties.Active.eq(true)
        )
                .orderAsc(ContentDao.Properties.Order)
                .list()
    }

    fun createContentAttempt(): LiveData<Resource<CourseAttempt>> {
        courseApiClient.createContentAttempt(content.value!!.id)
                .enqueue(object : TestpressCallback<CourseAttempt>() {
                    override fun onSuccess(result: CourseAttempt?) {
                        contentAttempt.value = Resource(Status.SUCCESS, result, null)
                    }

                    override fun onException(exception: TestpressException?) {
                        contentAttempt.value = Resource(Status.ERROR, null, exception)
                    }
                })
        return contentAttempt
    }

    private fun getContentFromDb(contentId: Int): Content? {
        val contents = contentDao.queryBuilder().where(ContentDao.Properties.Id.eq(contentId)).list()

        if (contents.isNotEmpty()) {
            return contents[0]
        }
        return null
    }

    fun getContent(position: Int, chapterId: Long): MutableLiveData<Content> {
        if (content.value == null) {
            val contents = getChapterContents(chapterId)
            content.value = contents[position]
        }
        return content
    }

    fun getContent(contentId: Int): MutableLiveData<Content> {
        if (content.value == null) {
            if (getContentFromDb(contentId) != null) {
                content.value = getContentFromDb(contentId)
            } else {
                loadContent(contentId)
            }
        }
        return content
    }

    fun storeBookmarkId(bookmarkId: Long?) {
        content.value?.bookmarkId = bookmarkId
        contentDao.updateInTx(content.value)
    }
}