package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.R
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.course.network.Resource
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.GreendaoCleanupMixin
import `in`.testpress.course.viewmodels.ContentViewModel
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.greendao.Chapter
import `in`.testpress.models.greendao.Content
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.support.v4.SupportFragmentController

@RunWith(RobolectricTestRunner::class)
class BaseContentDetailFragmentTest : GreendaoCleanupMixin() {
    lateinit var contentFragment: ConcreteContentFragment
    var content = Content()
    private val chapter = Chapter()
    private val contentDao =
        TestpressSDKDatabase.getContentDao(ApplicationProvider.getApplicationContext())

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(
            ApplicationProvider.getApplicationContext<Context>(),
            TestpressSession(instituteSettings, "USER_TOKEN")
        )
        setUpChapterAndContent()
        initializeContentFragment()
    }

    private fun setUpChapterAndContent() {
        chapter.id = 1
        chapter.name = "Chapter"
        content.title = "New Content"
        content.id = 1
        content.active = true
        content.chapter = chapter
        content.isLocked = false
        contentDao.insertOrReplaceInTx(content)
    }

    private fun initializeContentFragment() {
        val bundle = Bundle()
        bundle.putLong(ContentActivity.CONTENT_ID, 1)

        contentFragment = ConcreteContentFragment()
        contentFragment = spy(contentFragment)
        contentFragment.arguments = bundle

        SupportFragmentController.setupFragment(contentFragment)
        contentFragment.viewModel = mock(ContentViewModel::class.java)
    }

    @Test
    fun bookmarkFragmentShouldBeInitializedAfterInitializeContent() {
        val dbData = MutableLiveData(Resource.success(content.asDomainContent()))
        `when`(contentFragment.viewModel.getContent(1)).thenReturn(dbData)
        runBlocking {
            contentFragment.loadContentAndInitializeBoomarkFragment()
        }

        verify(contentFragment, atLeastOnce()).initializeBookmarkFragment()
    }

    @Test
    fun emptyViewFragmentShouldGetInitialized() {
        verify(contentFragment).initializeEmptyViewFragment()
    }

    @Test
    fun storeBookmarkIdToContentShouldGetCalledOnBookmarkSuccess() {
        contentFragment.onBookmarkSuccess(2)
        verify(contentFragment.viewModel).storeBookmarkIdToContent(2, 1)
    }

    @Test
    fun storeBookmarkIdToContentShouldGetCalledOnBookmarkDeletion() {
        contentFragment.onDeleteBookmarkSuccess()
        verify(contentFragment.viewModel).storeBookmarkIdToContent(null, 1)
    }

    @Test
    fun updateContentShouldDisplayContentOnSuccess() {
        reset(contentFragment)
        val dbData = MutableLiveData(Resource.success(content.asDomainContent()))
        `when`(contentFragment.viewModel.getContent(1, true)).thenReturn(dbData)
        contentFragment.updateContent()

        verify(contentFragment).display()
    }

    @Test
    fun onRetryClickUpdateContentShouldGetCalled() {
        doNothing().`when`(contentFragment).updateContent()
        contentFragment.onRetryClick()

        verify(contentFragment).updateContent()
    }

    class ConcreteContentFragment : BaseContentDetailFragment() {
        override var isBookmarkEnabled: Boolean = true

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.base_content_detail, container, false)
        }

        override fun display() {}
    }
}