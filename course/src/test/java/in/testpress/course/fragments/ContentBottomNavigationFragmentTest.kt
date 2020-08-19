package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse.PRODUCT_SLUG
import `in`.testpress.course.domain.DomainContent
import `in`.testpress.course.ui.ContentActivity.CONTENT_ID
import `in`.testpress.course.viewmodels.ContentViewModel
import `in`.testpress.models.InstituteSettings
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.support.v4.SupportFragmentController
import java.lang.reflect.Field

@RunWith(RobolectricTestRunner::class)
class ContentBottomNavigationFragmentTest {
    private val USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU"
    lateinit var contentFragment: ContentBottomNavigationFragment
    private val context = ApplicationProvider.getApplicationContext<Context>()
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
    private val data = MutableLiveData<Resource<DomainContent>>()
    private val content = DomainContent(1, chapterId = 1,
            active = true, contentType = "dummy", hasStarted = true,
            isLocked = false, isScheduled = false)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(context, TestpressSession(instituteSettings, USER_TOKEN))
        initializeContentFragment()
    }

    private fun initializeContentFragment() {
        val bundle = Bundle()
        bundle.putInt(CONTENT_ID, 1)
        bundle.putString(PRODUCT_SLUG, "product")
        data.value = Resource.success(content)

        contentFragment = ContentBottomNavigationFragment()
        contentFragment = spy(contentFragment)
        contentFragment.arguments = bundle
        SupportFragmentController.setupFragment(contentFragment)
        contentFragment.viewModel = mock(ContentViewModel::class.java)
        `when`(contentFragment.viewModel.getContent(anyLong(), anyBoolean())).thenReturn(data)
    }

    @Test
    fun testInitPrevButton() {
        contentFragment.initPrevButton(0, listOf())
        Assert.assertEquals(View.INVISIBLE, contentFragment.previousButton.visibility)

        contentFragment.initPrevButton(1, listOf())
        Assert.assertEquals(View.VISIBLE, contentFragment.previousButton.visibility)
    }

    @Test
    fun showMenuIfOnlyOneContentPresent() {
        val contents = MutableLiveData<List<DomainContent>>(listOf(content))
        contentFragment.content = content
        `when`(contentFragment.viewModel.getContentsForChapter(anyLong())).thenReturn(contents)
        contentFragment.initNextButton(0)
        Assert.assertEquals(View.VISIBLE, contentFragment.nextButton.visibility)
        Assert.assertEquals(context.getString(R.string.testpress_menu), contentFragment.nextButton.text)
    }

    @Test
    fun nextButtonVisibleIfManyContentsPresent() {
        val contents = MutableLiveData<List<DomainContent>>(listOf(content, content))
        contentFragment.content = content
        `when`(contentFragment.viewModel.getContentsForChapter(anyLong())).thenReturn(contents)
        contentFragment.initNextButton(0)
        Assert.assertEquals(View.VISIBLE, contentFragment.nextButton.visibility)
        Assert.assertEquals(context.getString(R.string.testpress_next_content), contentFragment.nextButton.text)
    }

    @Test
    fun testPageNumber() {
        val contents = MutableLiveData<List<DomainContent>>(listOf(content))
        `when`(contentFragment.viewModel.getContentsForChapter(anyLong())).thenReturn(contents)
        contentFragment.initializeAndShowNavigationButtons()

        Assert.assertEquals(contentFragment.pageNumber.text, "1/1")
        // verify(contentFragment).initNextButton(0)
        // verify(contentFragment).initPrevButton(0, listOf())
    }

    @After
    fun tearDown() {
        resetSingleton(TestpressSDKDatabase::class.java, "database")
        resetSingleton(TestpressSDKDatabase::class.java, "daoSession")
    }

    private fun resetSingleton(clazz: Class<TestpressSDKDatabase>, fieldName: String) {
        lateinit var instance: Field
        try {
            instance = clazz.getDeclaredField(fieldName)
            instance.isAccessible = true
            instance.set(null, null)
        } catch (e: Exception) {
            throw RuntimeException()
        }
    }
}