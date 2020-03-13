package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
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
import `in`.testpress.database.TestpressDatabase
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.network.RetrofitCall
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class ContentRepository(
    val context: Context
) {
    val roomContentDao = TestpressDatabase(context).contentDao()
    val contentDao = TestpressSDKDatabase.getContentDao(context)
    val courseNetwork = CourseNetwork(context)

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
        val contents =
            contentDao.queryBuilder().where(ContentDao.Properties.Id.eq(contentId)).list()

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


    open fun storeContentAndItsRelationsToDB(content: NetworkContent) {
        val greenDaoContent = content.asGreenDaoModel()
        content.exam?.let {
            val examDao = TestpressSDKDatabase.getExamDao(context)
            val exam = it.asGreenDaoModel()
            greenDaoContent.examId = exam.id
            examDao.insertOrReplace(exam)
        }
        content.htmlContent ?.let {
            val htmlContentDao = TestpressSDKDatabase.getHtmlContentDao(context)
            val htmlContent = it.asGreenDaoModel()
            greenDaoContent.htmlId = htmlContent.id
            htmlContentDao.insertOrReplace(htmlContent)
        }
        content.video?.let {
            val videoDao = TestpressSDKDatabase.getVideoDao(context)
            val video = it.asGreenDaoModel()
            greenDaoContent.videoId = video.id
            videoDao.insertOrReplace(it.asGreenDaoModel())
        }
        content.attachment?.let {
            val attachmentDao = TestpressSDKDatabase.getAttachmentDao(context)
            val attachment = it.asGreenDaoModel()
            greenDaoContent.attachmentId = attachment.id
            attachmentDao.insertOrReplace(it.asGreenDaoModel())
        }
        contentDao.insertOrReplace(greenDaoContent)
    }
}

class ContentRepositoryFactory {
    companion object {
        fun getRepository(contentType: String, context: Context): ContentRepository =
            when (contentType) {
                "Attachment" -> AttachmentContentRepository(context) as ContentRepository
                "Html" -> HtmlContentRepository(context) as ContentRepository
                "Notes" -> HtmlContentRepository(context) as ContentRepository
                "Video" -> VideoContentRepository(context) as ContentRepository
                "Exam" -> ExamContentRepository(context) as ContentRepository
                "Quiz" -> ExamContentRepository(context) as ContentRepository
                else -> ContentRepository(context)
            }
    }
}
