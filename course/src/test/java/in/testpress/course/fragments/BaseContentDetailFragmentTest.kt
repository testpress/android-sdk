package `in`.testpress.course.fragments

import `in`.testpress.core.TestpressSDKDatabase
import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.course.R
import `in`.testpress.course.TestpressCourse
import `in`.testpress.course.domain.asDomainContent
import `in`.testpress.course.ui.ContentActivity
import `in`.testpress.course.util.GreendaoCleanupMixin
import `in`.testpress.course.viewmodels.ContentViewModel
import `in`.testpress.models.InstituteSettings
import `in`.testpress.models.greendao.Chapter
import `in`.testpress.models.greendao.Content
import `in`.testpress.network.Resource
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
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
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.support.v4.SupportFragmentController

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class BaseContentDetailFragmentTest : GreendaoCleanupMixin() {
    lateinit var contentFragment: ConcreteContentFragment
    var content = Content()
    private val chapter = Chapter()
    private val contentDao =
        TestpressSDKDatabase.getContentDao(ApplicationProvider.getApplicationContext())
    private val chapterDao =
            TestpressSDKDatabase.getChapterDao(ApplicationProvider.getApplicationContext())

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
        chapterDao.insertOrReplaceInTx(chapter)
        contentDao.insertOrReplaceInTx(content)
    }

    private fun initializeContentFragment() {
        val bundle = Bundle()
        bundle.putLong(ContentActivity.CONTENT_ID, 1)
        bundle.putString(TestpressCourse.CONTENT_TYPE, "Attachment")

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
        contentFragment.isBookmarkEnabled = true
        runBlocking {
            contentFragment.loadContentAndInitializeBoomarkFragment()
        }

        assert(contentFragment.bookmarkFragment != null)
    }

    @Test
    fun emptyViewFragmentShouldGetInitialized() {
        assert(contentFragment.emptyViewFragment != null)
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
        contentFragment.forceReloadContent()

        verify(contentFragment).display()
    }

    @Test
    fun onRetryClickUpdateContentShouldGetCalled() {
        doNothing().`when`(contentFragment).forceReloadContent()
        contentFragment.onRetryClick()

        verify(contentFragment).forceReloadContent()
    }

    @Test
    fun onBackpressedContentListShouldBeForceRefreshed() {
        contentFragment.onBackPressed()

        val prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences(
                ContentActivity.TESTPRESS_CONTENT_SHARED_PREFS,
                Context.MODE_PRIVATE
        )
        assert(prefs.getBoolean(ContentActivity.FORCE_REFRESH, false))
    }

    class ConcreteContentFragment : BaseContentDetailFragment() {
        override var isBookmarkEnabled: Boolean = false

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