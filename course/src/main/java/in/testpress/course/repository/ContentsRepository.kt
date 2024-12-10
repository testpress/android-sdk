package `in`.testpress.course.repository

import `in`.testpress.core.TestpressCallback
import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asDomainContents
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.network.Resource
import `in`.testpress.models.greendao.Chapter
import `in`.testpress.models.greendao.ChapterDao
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.v2_4.models.ApiResponse
import `in`.testpress.v2_4.models.ContentsListResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ContentsRepository(val context: Context, val chapterId: Long = -1) {
    val contentDao = TestpressSDKDatabase.getContentDao(context)
    val chapterDao = TestpressSDKDatabase.getChapterDao(context)
    val courseNetwork = CourseNetwork(context)
    var page = 1
    val chapter: Chapter

    var _resourceContents: MutableLiveData<Resource<List<DomainContent>>> = MutableLiveData()
    val resourceContents: LiveData<Resource<List<DomainContent>>>
        get() = _resourceContents

    init {
        val contents = contentDao.queryBuilder()
            .where(ContentDao.Properties.ChapterId.eq(chapterId))
            .list()
        if (contents.isNotEmpty()) {
             val sortedContents = sortContentsByOrder(contents)
            _resourceContents.postValue(Resource.success(sortedContents.asDomainContents()))
        } else {
            _resourceContents.value = Resource.loading(null)
        }
        chapter = chapterDao.queryBuilder().where(ChapterDao.Properties.Id.eq(chapterId)).list()[0]
    }

    fun loadItems(page: Int = 1) {
        val url = chapter.chapterContentsUrl
        val queryParams = hashMapOf<String, Any>("page" to page)
        courseNetwork.getContents(url, queryParams)
            .enqueue(object : TestpressCallback<ApiResponse<ContentsListResponse>>(){
                override fun onSuccess(result: ApiResponse<ContentsListResponse>) {
                    handleFetchSuccess(result)
                }

                override fun onException(exception: TestpressException?) {
                    val contents = getAll()
                    if (contents?.isNotEmpty() == true) {
                        _resourceContents.postValue(Resource.error(exception!!, contents.asDomainContents()))
                    } else {
                        _resourceContents.postValue(Resource.error(exception!!, null))
                    }
                }
            })
    }

    private fun handleFetchSuccess(response: ApiResponse<ContentsListResponse>) {
        if (page == 1) {
            // Delete all contents of chapter once first page is fetched
            deleteExistingContents()
        }

        storeContent(response.results)
        val contents = contentDao.queryBuilder()
                .where(ContentDao.Properties.ChapterId.eq(chapterId))
                .list()
        val sortedContents = sortContentsByOrder(contents)
        _resourceContents.postValue(Resource.success(sortedContents.asDomainContents()))

        if (response.next != null) {
            page += 1
            loadItems(page)
        } else {
            page = 1
        }
    }

    private fun deleteExistingContents() {
        contentDao.queryBuilder()
            .where(ContentDao.Properties.ChapterId.eq(chapterId))
            .buildDelete()
            .executeDeleteWithoutDetachingEntities()
    }

    private fun getAll(): MutableList<Content>? {
        return contentDao.queryBuilder()
            .where(ContentDao.Properties.ChapterId.eq(chapterId))
            .orderAsc(ContentDao.Properties.Order)
            .list()
    }

    private fun storeContent(response: ContentsListResponse): List<DomainContent> {
        val examDao = TestpressSDKDatabase.getExamDao(context)
        examDao.insertOrReplaceInTx(response.exams)

        val htmlContentDao = TestpressSDKDatabase.getHtmlContentDao(context)
        htmlContentDao.insertOrReplaceInTx(response.notes)

        val attachmentDao = TestpressSDKDatabase.getAttachmentDao(context)
        attachmentDao.insertOrReplaceInTx(response.attachments)

        val streamDao = TestpressSDKDatabase.getStreamDao(context)
        streamDao.insertOrReplaceInTx(response.streams)

        val videoDao = TestpressSDKDatabase.getVideoDao(context)
        videoDao.insertOrReplaceInTx(response.videos)

        val videoConferenceDao = TestpressSDKDatabase.getVideoConferenceDao(context)
        videoConferenceDao.insertOrReplaceInTx(response.videoConferences)

        val liveStreamDao = TestpressSDKDatabase.getLiveStreamDao(context)
        liveStreamDao.insertOrReplaceInTx(response.liveStreams)

        val contentDao = TestpressSDKDatabase.getContentDao(context)
        contentDao.insertOrReplaceInTx(response.contents)

        return response.contents.asDomainContents()
    }

    private fun sortContentsByOrder(contents: List<Content>): List<Content> {
        return contents.sortedWith(compareBy {
            it.order
        })
    }
}
