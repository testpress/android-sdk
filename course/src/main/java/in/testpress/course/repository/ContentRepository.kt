package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.course.api.TestpressCourseApiClient.CONTENTS_PATH_v2_4
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.course.domain.asDomainContents
import `in`.testpress.course.enums.Status
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.NetworkContentAttempt
import `in`.testpress.course.network.Resource
import `in`.testpress.course.network.asDatabaseModel
import `in`.testpress.course.network.asGreenDaoModel
import `in`.testpress.models.greendao.AttachmentDao
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.network.RetrofitCall
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ContentRepository(
    val roomContentDao: `in`.testpress.database.ContentDao,
    val contentDao: ContentDao,
    val courseNetwork: CourseNetwork
) {
    private var contentAttempt: MutableLiveData<Resource<NetworkContentAttempt>> = MutableLiveData()

    fun loadContent(
        contentId: Long,
        forceRefresh: Boolean = false
    ): LiveData<Resource<DomainContent>> {
        return object : NetworkBoundResource<DomainContent, NetworkContent>() {
            override fun saveNetworkResponseToDB(item: NetworkContent) {
                roomContentDao.insert(item.asDatabaseModel())
                contentDao.insertOrReplace(item.asGreenDaoModel())
            }

            override fun shouldFetch(data: DomainContent?): Boolean {
                return forceRefresh || getContentFromDB(contentId) == null
            }

            override fun loadFromDb(): LiveData<DomainContent> {
                val liveData = MutableLiveData<DomainContent>()
                liveData.postValue(getContentFromDB(contentId)?.asDomainContent())
                return liveData
            }

            override fun createCall(): RetrofitCall<NetworkContent> {
                val contentUrl = "$CONTENTS_PATH_v2_4$contentId/"
                return courseNetwork.getNetworkContent(contentUrl)
            }
        }.asLiveData()
    }

    fun getContentInChapterForPosition(position: Int, chapterId: Long): DomainContent {
        val contents = contentDao.queryBuilder()
            .where(
                ContentDao.Properties.ChapterId.eq(chapterId),
                ContentDao.Properties.Active.eq(true)
            )
            .orderAsc(ContentDao.Properties.Order)
            .list()
        return contents[position].asDomainContent()
    }

    fun getContentsForChapterFromDB(chapterId: Long): LiveData<List<DomainContent>>? {
        val contentsLiveData = MutableLiveData<List<DomainContent>>()
        val contents = contentDao.queryBuilder()
            .where(
                ContentDao.Properties.ChapterId.eq(chapterId),
                ContentDao.Properties.Active.eq(true)
            ).list().asDomainContents()
        contentsLiveData.value = contents
        return contentsLiveData
    }

    fun getContentFromDB(contentId: Long): Content? {
        val contents = contentDao.queryBuilder().where(ContentDao.Properties.Id.eq(contentId)).list()

        if (contents.isEmpty()) {
            return null
        }
        return contents[0]
    }

    fun createContentAttempt(contentId: Long): LiveData<Resource<NetworkContentAttempt>> {
        courseNetwork.createContentAttempt(contentId)
            .enqueue(object : TestpressCallback<NetworkContentAttempt>() {
                override fun onSuccess(result: NetworkContentAttempt) {
                    contentAttempt.value = Resource(Status.SUCCESS, result, null)
                }

                override fun onException(exception: TestpressException?) {
                    contentAttempt.value = Resource(Status.ERROR, null, exception)
                }
            })
        return contentAttempt
    }

    fun storeBookmarkIdToContent(bookmarkId: Long?, contentId: Long) {
        val content = getContentFromDB(contentId)
        content?.bookmarkId = bookmarkId
        contentDao.updateInTx(content)
    }
}