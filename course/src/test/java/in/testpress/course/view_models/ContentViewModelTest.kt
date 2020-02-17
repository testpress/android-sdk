package `in`.testpress.course.view_models

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.ui.view_models.ContentViewModel
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.greendao.Chapter
import `in`.testpress.models.greendao.Content
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.*
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import junit.framework.Assert
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.lang.reflect.Field


@RunWith(RobolectricTestRunner::class)
class ContentViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    lateinit var viewModel: ContentViewModel
    private val USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU"
    private lateinit var mockWebServer: MockWebServer
    var content = Content()


    fun setUpChapterAndContent() {
        val contentDao = TestpressSDKDatabase.getContentDao(ApplicationProvider.getApplicationContext())
        val chapter = Chapter()
        chapter.id = 1
        chapter.name = "Chapter"
        content.title = "New Content"
        content.id = 1
        content.active = true
        content.chapter = chapter
        contentDao.insertOrReplaceInTx(content)
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockWebServer = MockWebServer()
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext<Context>(),
                TestpressSession(instituteSettings, USER_TOKEN))
        viewModel = ContentViewModel(getApplicationContext())
        viewModel = spy(viewModel)
        mockWebServer.start(9200)
    }

    @Test
    fun testGetChapterContents() {
        Assert.assertEquals(viewModel.getChapterContents(1).size, 1)
        Assert.assertEquals(viewModel.getChapterContents(2).size, 0)
    }

    @Test
    fun testGetContentFromDb() {
        viewModel.getContent(content.id.toInt()).observeOnce {
            Assert.assertEquals(content.id, it.id)
        }
    }

    @Test
    fun testGetContentWithChapterIdAndPosition() {
        viewModel.getContent(0, 1).observeOnce {
            Assert.assertEquals(content.id, it.id)
        }
    }

    @Test
    fun testLoadContent() {
        viewModel.getContent(1000)
        verify(viewModel).loadContent(1000)
    }

    @Test
    fun testStoreBookmarkId() {
        viewModel.storeBookmarkId(24)
        viewModel.getContent(1).observeOnce {
            Assert.assertEquals(it.bookmarkId, 24)
        }
    }

    @After
    fun tearDown() {
        viewModel.content = MutableLiveData()
        mockWebServer.shutdown()
        resetSingleton(TestpressSDKDatabase::class.java, "database");
        resetSingleton(TestpressSDKDatabase::class.java, "daoSession");
    }

    fun resetSingleton(clazz: Class<TestpressSDKDatabase>, fieldName: String) {
        lateinit var instance: Field
        try {
            instance = clazz.getDeclaredField(fieldName);
            instance.isAccessible = true;
            instance.set(null, null);
        } catch (e: Exception) {
            throw RuntimeException()
        }
    }
}


fun <T> LiveData<T>.observeOnce(onChangeHandler: (T) -> Unit) {
    val observer = OneTimeObserver(onChangeHandler)
    observe(observer, observer)
}

class OneTimeObserver<T>(private val handler: (T) -> Unit) : Observer<T>, LifecycleOwner {
    private val lifecycle = LifecycleRegistry(this)

    init {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun onChanged(t: T?) {
        handler(t!!)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

