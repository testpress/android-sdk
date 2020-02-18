package `in`.testpress.course.ui.fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.ui.fragments.content_fragments.HtmlContentFragment
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.greendao.Chapter
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.HtmlContent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import junit.framework.Assert
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil
import java.lang.reflect.Field


@RunWith(RobolectricTestRunner::class)
class HtmlContentFragmentTest {
    private val USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU"
    lateinit var contentFragment: HtmlContentFragment
    var content = Content()
    val chapter = Chapter()
    val contentDao = TestpressSDKDatabase.getContentDao(ApplicationProvider.getApplicationContext())

    fun setUpChapterAndContent() {
        val htmlContent = HtmlContent()
        htmlContent.id = 1
        chapter.id = 1
        chapter.name = "Chapter"
        content.title = "New Content"
        content.id = 1
        content.active = true
        content.chapter = chapter
        content.isLocked = false
        content.htmlContent = htmlContent
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

        contentFragment = HtmlContentFragment()
        contentFragment = Mockito.spy(contentFragment)
        contentFragment.arguments = bundle
        val intent = Intent()
        SupportFragmentTestUtil.startFragment(contentFragment, FragmentActivity::class.java)

        SupportFragmentTestUtil.startFragment(contentFragment)
        contentFragment.contentDao = TestpressSDKDatabase.getContentDao(getApplicationContext())
        contentFragment.htmlContentDao = TestpressSDKDatabase.getHtmlContentDao(getApplicationContext())
    }

    @Before
    fun setUpMockito() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testOnUpdateContent() {
        Assert.assertTrue(contentFragment.htmlContentDao.queryBuilder().list().isEmpty())

        contentFragment.onUpdateContent(content)

        Assert.assertFalse(contentFragment.htmlContentDao.queryBuilder().list().isEmpty())
    }

    @Test
    fun testLoadContent() {
        content.htmlContent = null
        contentDao.insertOrReplaceInTx(content)
        contentFragment.loadContent()

        verify(contentFragment).updateContent()
        Assert.assertEquals(contentFragment.titleView.text, content.title)
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