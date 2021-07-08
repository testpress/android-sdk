package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.network.Resource
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.GreendaoCleanupMixin
import `in`.testpress.course.viewmodels.ContentViewModel
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.greendao.Chapter
import `in`.testpress.models.greendao.Content
import `in`.testpress.models.greendao.HtmlContent
import `in`.testpress.util.WebViewUtils
import android.content.Context
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.support.v4.SupportFragmentController

@RunWith(RobolectricTestRunner::class)
class HtmlContentFragmentTest: GreendaoCleanupMixin() {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()
    var content = Content()
    private val chapter = Chapter()
    private val contentDao =
        TestpressSDKDatabase.getContentDao(ApplicationProvider.getApplicationContext())
    lateinit var contentFragment: HtmlContentFragment

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(
            ApplicationProvider.getApplicationContext<Context>(),
            TestpressSession(instituteSettings, "USER_TOKEN")
        )
    }

    fun setUpChapterAndContent(htmlContent: HtmlContent? = null) {
        chapter.id = 1
        chapter.name = "Chapter"
        content.title = "New Content"
        content.htmlContent = htmlContent
        content.id = 1
        content.active = true
        content.chapter = chapter
        content.isLocked = false
        contentDao.insertOrReplaceInTx(content)
    }

    private fun initializeContentFragment() {
        val bundle = Bundle()
        bundle.putLong(ContentActivity.CONTENT_ID, 1)

        contentFragment = HtmlContentFragment()
        contentFragment = Mockito.spy(HtmlContentFragment::class.java)
        contentFragment.arguments = bundle
        doNothing().`when`(contentFragment).forceReloadContent()
//        SupportFragmentController.setupFragment(contentFragment)

        contentFragment.viewModel = Mockito.mock(ContentViewModel::class.java)
        contentFragment.webViewUtils = Mockito.mock(WebViewUtils::class.java)
        val dbData = MutableLiveData(Resource.success(content.asDomainContent()))
        `when`(contentFragment.viewModel.getContent(1, true)).thenReturn(dbData)
    }

    @Test
    fun displayShouldInitWebviewWithHtml() {
//        val html = HtmlContent(1)
//        html.textHtml = "Hello World"
//        setUpChapterAndContent(html)
//        initializeContentFragment()
//        contentFragment.display()
//
//        var expectedHtml = """
//            <div style='padding-left: 20px; padding-right: 20px;'>
//                ${html.textHtml}
//            </div>
//        """.trimIndent()
//        verify(contentFragment.webViewUtils).initWebView(expectedHtml, contentFragment.activity)
    }
}