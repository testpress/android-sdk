package `in`.testpress.course.repository

import `in`.testpress.course.models.Resource
import `in`.testpress.course.network.TestpressCourseApiClient
import `in`.testpress.course.util.RetrofitCallMock
import `in`.testpress.course.util.mock
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.ContentDao
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import org.greenrobot.greendao.query.QueryBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ContentRepositoryTest {
    private val contentDao = mock(ContentDao::class.java)
    private val courseApiClient = mock(TestpressCourseApiClient::class.java)
    private val repo = ContentRepository(contentDao, courseApiClient)
    @Mock
    lateinit var queryBuilder: QueryBuilder<Content>

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

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
        verify(courseApiClient, never()).getContent(anyString())
    }

    @Test
    fun fetchFromNetwork() {
        val dbData = arrayListOf<Content>(Content())
        `when`(contentDao.queryBuilder().where(any()).list()).thenReturn(dbData)
        val apiCall = RetrofitCallMock(Resource.success(Content(1)))
        `when`(courseApiClient.getContent(anyString())).thenReturn(apiCall)
        val observer = mock<Observer<Resource<Content>>>()

        repo.loadContent(1, true).observeForever(observer)
        verify(courseApiClient).getContent(anyString())
    }

    @Test
    fun getChapterContentsFromDB() {
        val dbData = arrayListOf<Content>(Content())
        `when`(contentDao.queryBuilder().list()).thenReturn(dbData)
        val result = repo.getChapterContentsFromDB(1)

        assert(dbData == result)
    }

    @Test
    fun getContent() {
        val dbData = arrayListOf<Content>(Content())
        `when`(contentDao.queryBuilder().list()).thenReturn(dbData)
        val result = repo.getContent(0, 1)

        assert(dbData[0] == result)
    }
}