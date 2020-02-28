package `in`.testpress.course.repository

import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.course.network.CourseNetwork
import `in`.testpress.course.network.NetworkContent
import `in`.testpress.course.network.Resource
import `in`.testpress.course.network.asDatabaseModel
import `in`.testpress.course.util.RetrofitCallMock
import `in`.testpress.course.util.getOrAwaitValue
import `in`.testpress.course.util.mock
import `in`.testpress.database.ContentEntity
import `in`.testpress.models.greendao.ContentDao
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
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
    private val courseNetwork = mock(CourseNetwork::class.java)
    private val repo = ContentRepository(roomContentDao, contentDao, courseNetwork)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    fun createContent(): NetworkContent {
        return NetworkContent(
            id = 1, title = "Content", active = true,
            order = 0, contentType = "exam", isLocked = false,
            isScheduled = false, hasStarted = false
        )
    }

    @Test
    fun loadUser() {
        repo.loadContent(1)

        verify(roomContentDao, atLeastOnce()).findById(1)
        verify(courseNetwork, never()).getNetworkContent(anyString())
    }

    fun getLiveData(content: Any): LiveData<Any> {
        val mutableLiveData = MutableLiveData<Any>()
        mutableLiveData.value = content
        return mutableLiveData
    }

    @Test
    fun fetchFromNetwork() {
        val dbData = getLiveData(createContent().asDatabaseModel()) as LiveData<ContentEntity>
        `when`(roomContentDao.findById(ArgumentMatchers.anyLong())).thenReturn(dbData)
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
        val dbData =
            getLiveData(arrayListOf(createContent().asDatabaseModel())) as LiveData<List<ContentEntity>>
        `when`(roomContentDao.getChapterContents(ArgumentMatchers.anyLong())).thenReturn(dbData)
        val result = repo.getChapterContentsFromDB(1)?.getOrAwaitValue()

        assert(result == dbData.value?.asDomainContent())
    }
}