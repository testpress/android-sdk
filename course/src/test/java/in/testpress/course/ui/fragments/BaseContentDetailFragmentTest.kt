package `in`.testpress.course.ui.fragments

import `in`.testpress.core.TestpressException
import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.enums.Status
import `in`.testpress.course.models.Resource
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.greendao.Chapter
import `in`.testpress.models.greendao.Content
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
class BaseContentDetailFragmentTest {
    private val USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU"

    lateinit var contentFragment: SampleConcreteContentFragment

    @Mock
    lateinit var exception: TestpressException

    var content = Content()
    val chapter = Chapter()
    val contentDao = TestpressSDKDatabase.getContentDao(ApplicationProvider.getApplicationContext())

    fun setUpChapterAndContent() {
        chapter.id = 1
        chapter.name = "Chapter"
        content.title = "New Content"
        content.id = 1
        content.active = true
        content.chapter = chapter
        content.isLocked = false
        contentDao.insertOrReplaceInTx(content)
    }

    @Before
    fun setUp() {
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext<Context>(),
                TestpressSession(instituteSettings, USER_TOKEN))
        setUpChapterAndContent()
        initializeContentFragment()
    }

    fun initializeContentFragment() {
        val bundle = Bundle()
        bundle.putLong(ContentActivity.CHAPTER_ID, 1)
        bundle.putInt(ContentActivity.POSITION, 0)

        contentFragment = SampleConcreteContentFragment()
        contentFragment = spy(contentFragment)
        contentFragment.arguments = bundle

        val fragmentManager = FragmentActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(contentFragment, null)
        fragmentTransaction.commitAllowingStateLoss()
        contentFragment.viewModel = spy(contentFragment.viewModel)
    }

    @Before
    fun setUpMockito() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testContentOnParseIntentArgs() {
        contentFragment.parseIntentArguments()
        Assert.assertNotNull(contentFragment.viewModel.content.value)
    }

    @Test
    fun testUpdateContent() {
        val resource: MutableLiveData<Resource<Content>> = MutableLiveData()
        resource.value = Resource(Status.SUCCESS, content, null)
        `when`(contentFragment.viewModel.loadContent(anyInt())).thenReturn(resource)
        contentFragment.updateContent()

        verify(contentFragment).onUpdateContent(content)
    }

    @Test
    fun testUpdateContentFailure() {
        val resource: MutableLiveData<Resource<Content>> = MutableLiveData()
        resource.value = Resource(Status.ERROR, null, exception)
        `when`(contentFragment.viewModel.loadContent(anyInt())).thenReturn(resource)
        contentFragment.updateContent()

        verify(contentFragment).handleError(exception)
    }

    @Test
    fun testOnBookmarkSuccess() {
        contentFragment.onBookmarkSuccess(1)
        verify(contentFragment.viewModel).storeBookmarkId(1)
    }

    @Test
    fun testOnDeleteBookmarkSuccess() {
        contentFragment.onDeleteBookmarkSuccess()
        verify(contentFragment.viewModel).storeBookmarkId(null)
    }

    @Test
    fun testInitNavigationButtons() {
        verify(contentFragment).initNavigationButtons()
        verify(contentFragment).initPrevButton()
        verify(contentFragment).initNextButton()
    }

    @Test
    fun testInitPrevButton() {
        contentFragment.initPrevButton()

        Assert.assertEquals(contentFragment.previousButton.visibility, View.INVISIBLE)

        contentFragment.position = 1
        contentFragment.initPrevButton()
        Assert.assertEquals(contentFragment.previousButton.visibility, View.VISIBLE)
    }

    @Test
    fun testInitNextButton() {
        contentFragment.initNextButton()

        Assert.assertEquals(contentFragment.nextButton.visibility, View.VISIBLE)
        Assert.assertEquals(contentFragment.nextButton.text, "Menu")

        `when`(contentFragment.viewModel.getChapterContents(1)).thenReturn(listOf(content, content))
        contentFragment.initNextButton()
        Assert.assertEquals(contentFragment.nextButton.text, "Next")
    }

    @After
    fun tearDown() {
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