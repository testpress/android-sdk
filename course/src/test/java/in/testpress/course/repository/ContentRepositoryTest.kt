package `in`.testpress.course.repository

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.course.domain.asDomainContents
import `in`.testpress.course.network.*
import `in`.testpress.network.Resource
import `in`.testpress.course.util.RetrofitCallMock
import `in`.testpress.course.util.getOrAwaitValue
import `in`.testpress.course.util.mock
import `in`.testpress.models.greendao.AttachmentDao
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import `in`.testpress.models.greendao.ExamDao
import `in`.testpress.models.greendao.HtmlContentDao
import `in`.testpress.models.greendao.VideoDao
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.runBlocking
import org.greenrobot.greendao.query.QueryBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ContentRepositoryTest {
    private val contentDao = mock(ContentDao::class.java)
    private val roomContentDao = mock(`in`.testpress.database.ContentDao::class.java)
    private val attachmentDao = mock(AttachmentDao::class.java)
    private val htmlContentDao = mock(HtmlContentDao::class.java)
    private val videoDao = mock(VideoDao::class.java)
    private val examDao = mock(ExamDao::class.java)
    private val courseNetwork = mock(CourseNetwork::class.java)
    @Mock
    lateinit var context: Context
    private val repo = ContentRepository(context)

    @Mock
    lateinit var queryBuilder: QueryBuilder<Content>

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    fun createContent(
        attachment: NetworkAttachmentContent? = null,
        htmlContent: NetworkHtmlContent? = null
    ): NetworkContent {
        return NetworkContent(
            id = 1, title = "Content", active = true,
            order = 0, contentType = "exam", isLocked = false,
            isScheduled = false, hasStarted = false, attachment = attachment,
            htmlContent = htmlContent
        )
    }

    private fun createContentAttempt(): NetworkContentAttempt {
        return NetworkContentAttempt(1)
    }

    @Before
    fun setUp() {
        `when`(contentDao.queryBuilder()).thenReturn(queryBuilder)
        `when`(contentDao.queryBuilder().where(any(), any())).thenReturn(queryBuilder)
        `when`(contentDao.queryBuilder().orderAsc(any())).thenReturn(queryBuilder)
    }

    @Test
    fun loadUser() {
        repo.loadContent(1)

        verify(contentDao, atLeastOnce()).queryBuilder()
        verify(courseNetwork, never()).getNetworkContent(anyString())
    }

    fun getLiveData(content: Any): LiveData<Any> {
        val mutableLiveData = MutableLiveData<Any>()
        mutableLiveData.value = content
        return mutableLiveData
    }

    @Test
    fun fetchFromNetwork() {
        val dbData = arrayListOf(Content(1))
        `when`(contentDao.queryBuilder().where(any()).list()).thenReturn(dbData)
        val apiCall = RetrofitCallMock(Resource.success(createContent()))
        `when`(courseNetwork.getNetworkContent(anyString())).thenReturn(apiCall)
        val observer = mock<Observer<Resource<DomainContent>>>()

        runBlocking {
            repo.loadContent(1, true).observeForever(observer)
        }
        verify(courseNetwork).getNetworkContent(anyString())
    }

    @Test
    fun getChapterContentsFromDB() {
        val dbData = listOf(Content(1))
        `when`(contentDao.queryBuilder().list()).thenReturn(dbData)
        val result = repo.getContentsForChapterFromDB(1)?.getOrAwaitValue()

        assert(result == dbData.asDomainContents())
    }

    @Test
    fun contentShouldBeFetchedFromDB() {
        val content = Content(1)
        val dbData = listOf(content)
        `when`(contentDao.queryBuilder().list()).thenReturn(dbData)
        val result = repo.getContentFromDB(1)

        assert(result == content)
    }

    @Test
    fun getContentWithPositionAndChapterIdShouldReturnContent() {
        val content = Content(1)
        content.chapterId = 2
        val content2 = Content(2)
        content2.chapterId = content.chapterId
        val dbData = listOf(content, content2)
        `when`(contentDao.queryBuilder().list()).thenReturn(dbData)
        val result = repo.getContentInChapterForPosition(1, content.chapterId)

        assert(result.value == dbData[1].asDomainContent())
    }

    @Test
    fun createAttemptShouldMakeAPICall() {
        val apiCall = RetrofitCallMock(Resource.success(createContentAttempt()))
        `when`(courseNetwork.createContentAttempt(1)).thenReturn(apiCall)
        val result = repo.createContentAttempt(1).getOrAwaitValue()

        verify(courseNetwork).createContentAttempt(1)
        assert(result == apiCall.resource)
    }

    @Test
    fun contentAndItsRelatedDataShouldBeStoredInDB() {
        val attachment = NetworkAttachmentContent(5)
        val htmlContent = NetworkHtmlContent(10)
        val content = createContent(attachment, htmlContent)
        repo.storeContentAndItsRelationsToDB(content)
        val contentArgumentCaptor = ArgumentCaptor.forClass(Content::class.java)

        verify(attachmentDao).insertOrReplace(any())
        verify(contentDao).insertOrReplace(contentArgumentCaptor.capture())

        val storedContent = contentArgumentCaptor.allValues[0]
        assert(attachment.id == storedContent.attachmentId)
        assert(htmlContent.id == storedContent.htmlId)
    }
}