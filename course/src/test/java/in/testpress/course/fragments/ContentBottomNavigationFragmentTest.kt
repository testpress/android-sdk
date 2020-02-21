package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.R
import `in`.testpress.course.models.Resource
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.ui.fragments.ContentBottomNavigationFragment
import `in`.testpress.course.ui.view_models.ContentViewModel
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.greendao.Content
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.test.core.app.ApplicationProvider
import junit.framework.Assert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
class ContentBottomNavigationFragmentTest {
    private val USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU"
    lateinit var contentFragment: ContentBottomNavigationFragment
    private val context = ApplicationProvider.getApplicationContext<Context>()
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
    val data = MutableLiveData<Resource<Content>>()
    val content = Content(1)

    @Before
    fun setUp() {
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(context, TestpressSession(instituteSettings, USER_TOKEN))
        initializeContentFragment()
    }

    @Before
    fun setUpMockito() {
        MockitoAnnotations.initMocks(this)
    }

    fun initializeContentFragment() {
        val bundle = Bundle()
        bundle.putInt(CONTENT_ID, 1)
        data.value = Resource.success(content)
        content.chapterId = 1
        content.isLocked = false

        contentFragment = ContentBottomNavigationFragment()
        contentFragment = spy(contentFragment)
        contentFragment.arguments = bundle
        SupportFragmentTestUtil.startVisibleFragment(contentFragment)
        contentFragment.viewModel = mock(ContentViewModel::class.java)
        `when`(contentFragment.viewModel.getContent(anyInt(), anyBoolean())).thenReturn(data)
        `when`(contentFragment.viewModel.getChapterContents(anyLong())).thenReturn(listOf())

    }

    @Test
    fun testInitPrevButton() {
        contentFragment.initPrevButton(0)
        Assert.assertEquals(View.INVISIBLE, contentFragment.previousButton.visibility)

        contentFragment.initPrevButton(1)
        Assert.assertEquals(View.VISIBLE, contentFragment.previousButton.visibility)
    }

    @Test
    fun testInitNextButton() {
        val contents = arrayListOf(content)
        contentFragment.content = content
        contentFragment.initNextButton(0)
        Assert.assertEquals(View.INVISIBLE, contentFragment.nextButton.visibility)

        `when`(contentFragment.viewModel.getChapterContents(anyLong())).thenReturn(contents)
        contentFragment.initNextButton(0)
        Assert.assertEquals(View.VISIBLE, contentFragment.nextButton.visibility)
        Assert.assertEquals(context.getString(R.string.testpress_menu), contentFragment.nextButton.text)

        contents.add(content)
        `when`(contentFragment.viewModel.getChapterContents(anyLong())).thenReturn(contents)
        contentFragment.initNextButton(0)
        Assert.assertEquals(View.VISIBLE, contentFragment.nextButton.visibility)
        Assert.assertEquals(context.getString(R.string.testpress_next_content), contentFragment.nextButton.text)
    }

    @Test
    fun testInitNavigationButtons() {
        `when`(contentFragment.viewModel.getChapterContents(anyLong())).thenReturn(listOf(content))
        contentFragment.initNavigationButtons()

        Assert.assertEquals(contentFragment.pageNumber.text, "1/1")
        verify(contentFragment).initNextButton(0)
        verify(contentFragment).initPrevButton(0)
    }


    @After
    fun tearDown() {
        resetSingleton(TestpressSDKDatabase::class.java, "database");
        resetSingleton(TestpressSDKDatabase::class.java, "daoSession");
    }

    private fun resetSingleton(clazz: Class<TestpressSDKDatabase>, fieldName: String) {
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