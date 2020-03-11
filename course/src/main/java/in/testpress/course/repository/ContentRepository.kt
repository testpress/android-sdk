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
import `in`.testpress.database.AttachmentContentDao
import `in`.testpress.database.ContentEntity
import `in`.testpress.database.ExamContentDao
import `in`.testpress.database.VideoContentDao
import `in`.testpress.models.greendao.AttachmentDao
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.models.greendao.ExamDao
import `in`.testpress.models.greendao.HtmlContentDao
import `in`.testpress.models.greendao.VideoDao
import `in`.testpress.network.RetrofitCall
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContentRepository(
    val roomContentDao: `in`.testpress.database.ContentDao,
    val contentDao: ContentDao,
    val attachmentDao: AttachmentDao,
    val htmlContentDao: HtmlContentDao,
    val videoContentDao: VideoDao,
    val examContentDao: ExamDao,
    val courseNetwork: CourseNetwork,
    val roomAttachmentDao: AttachmentContentDao,
    val roomHtmlContentDao: `in`.testpress.database.HtmlContentDao,
    val roomVideoDao: VideoContentDao,
    val roomExamDao: ExamContentDao
) {
    private var contentAttempt: MutableLiveData<Resource<NetworkContentAttempt>> = MutableLiveData()

    fun loadContent(
        contentId: Long,
        forceRefresh: Boolean = false
    ): LiveData<Resource<DomainContent>> {
        return object : NetworkBoundResource<DomainContent, NetworkContent>() {
            override fun saveNetworkResponseToDB(item: NetworkContent) {
                roomContentDao.insert(item.asDatabaseModel())
                storeContentAndItsRelationsToDB(item)
            }

            override fun shouldFetch(data: DomainContent?): Boolean {
                return forceRefresh || data == null
            }

            override fun loadFromDb(): LiveData<DomainContent> {
                return Transformations.map(roomContentDao.findById(contentId)){
                    it?.asDomainContent()
                }
            }

            override fun createCall(): RetrofitCall<NetworkContent> {
                val contentUrl = "$CONTENTS_PATH_v2_4$contentId/"
                return courseNetwork.getNetworkContent(contentUrl)
            }
        }.asLiveData()
    }

    fun getContentInChapterForPosition(position: Int, chapterId: Long): LiveData<DomainContent> {
        val liveData = MutableLiveData<DomainContent>()
        val contents = contentDao.queryBuilder()
            .where(
                ContentDao.Properties.ChapterId.eq(chapterId),
                ContentDao.Properties.Active.eq(true)
            )
            .orderAsc(ContentDao.Properties.Order)
            .list()
        liveData.postValue(contents[position].asDomainContent())
        return liveData
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
        if (contentAttempt.value == null || contentAttempt.value!!.status != Status.SUCCESS) {
            courseNetwork.createContentAttempt(contentId)
                .enqueue(object : TestpressCallback<NetworkContentAttempt>() {
                    override fun onSuccess(result: NetworkContentAttempt) {
                        contentAttempt.value = Resource(Status.SUCCESS, result, null)
                    }

                    override fun onException(exception: TestpressException?) {
                        contentAttempt.value = Resource(Status.ERROR, null, exception)
                    }
                })
        }
        return contentAttempt
    }

    fun storeContentAndItsRelationsToDB(content: NetworkContent) {
        storeAttachmentContent(content)
        storeHtmlContent(content)
        storeVideoContent(content)
        storeExamContent(content)
    }

    fun storeAttachmentContent(content: NetworkContent) {
        content.attachment?.let {
            val greenDaoContent = content.asGreenDaoModel()
            val roomContent = content.asDatabaseModel()
            val attachment = it.asGreenDaoModel()
            greenDaoContent.attachmentId = attachment.id
            attachmentDao.insertOrReplace(it.asGreenDaoModel())
            roomContent.attachmentId = attachment.id
            roomAttachmentDao.insert(it.asDatabaseModel())
            storeContent(greenDaoContent, roomContent)
        }
    }

    fun storeHtmlContent(content: NetworkContent) {
        content.htmlContent ?.let {
            val greenDaoContent = content.asGreenDaoModel()
            val htmlContent = it.asGreenDaoModel()
            greenDaoContent.htmlId = htmlContent.id
            htmlContentDao.insertOrReplace(htmlContent)

            val roomContent = content.asDatabaseModel()
            roomContent.htmlId = it.id
            roomHtmlContentDao.insert(it.asDatabaseModel())
            storeContent(greenDaoContent, roomContent)
        }
    }

    fun storeVideoContent(content: NetworkContent) {
        content.video?.let {
            val greenDaoContent = content.asGreenDaoModel()
            val video = it.asGreenDaoModel()
            greenDaoContent.videoId = video.id
            videoContentDao.insertOrReplace(video)

            val roomContent = content.asDatabaseModel()
            roomContent.videoId = it.id
            roomVideoDao.insert(it.asDatabaseModel())
            storeContent(greenDaoContent, roomContent)
        }
    }

    fun storeExamContent(content: NetworkContent) {
        content.exam?.let {
            val greenDaoContent = content.asGreenDaoModel()
            val exam = it.asGreenDaoModel()
            greenDaoContent.examId = exam.id
            examContentDao.insertOrReplace(exam)

            val roomContent = content.asDatabaseModel()
            roomContent.examId = exam.id
            roomExamDao.insert(it.asDatabaseModel())
            storeContent(greenDaoContent, roomContent)
        }
    }

    fun storeContent(greenDaoContent: Content, roomContent: ContentEntity) {
        contentDao.insertOrReplace(greenDaoContent)
        roomContentDao.insert(roomContent)
    }

    fun storeBookmarkIdToContent(bookmarkId: Long?, contentId: Long) {
        val content = getContentFromDB(contentId)
        content?.bookmarkId = bookmarkId
        contentDao.updateInTx(content)
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val roomContent = roomContentDao.findById(contentId)
                Transformations.map(roomContent) {
                    val content = it.content
                    content.bookmarkId = bookmarkId
                    roomContentDao.insert(content)
                }
            }
        }
    }
}