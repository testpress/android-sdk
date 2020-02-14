package `in`.testpress.course.ui.fragments

import `in`.testpress.core.TestpressSdk
import `in`.testpress.core.TestpressSession
import `in`.testpress.exam.network.TestpressExamApiClient
import `in`.testpress.models.InstituteSettings
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import kotlinx.android.synthetic.main.bookmark_fragment_layout.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner

import org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.junit.Rule
import org.mockito.Mockito
import org.mockito.MockitoAnnotations





@RunWith(RobolectricTestRunner::class)
class BookmarkFragmentTest {
    @Mock
    lateinit var layoutInflater: LayoutInflater
    @Mock
    lateinit var view: View
    @Mock
    lateinit var bookmarkListener: BookmarkListener

    lateinit var bookmarkFragment: BookmarkFragment
    private val USER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6NDYsInVzZXJfaWQiOjQ2LCJlbWFpbCI6IiIsImV4cCI6MTUxOTAzNjUzM30.FUuyJfYNSAw_VcypZsN8_ZHvZra6gHU3njcXmr-TGVU"

    @Before
    fun setUpMockito() {
        MockitoAnnotations.initMocks(this)
    }

    @After
    fun tearDownMockito() {
        Mockito.validateMockitoUsage()
    }

    @Before
    fun setUp() {
        val instituteSettings = InstituteSettings("http://localhost:9200")
        TestpressSdk.setTestpressSession(ApplicationProvider.getApplicationContext<Context>(),
                TestpressSession(instituteSettings, USER_TOKEN))
        bookmarkFragment = BookmarkFragment()
        var bookmarkFragmentActivity = BookmarkFragmentActivity()
        startFragment(bookmarkFragment, BookmarkFragmentActivity::class.java)
    }

    @Test
    fun testOnCreateView() {
        val createdView = bookmarkFragment.onCreateView(layoutInflater, null, null)
//        bookmarkFragment.onViewCreated(createdView!!, null)
        bookmarkFragment.initializeAdapters()
    }
}

class BookmarkFragmentActivity: FragmentActivity(), BookmarkListener {
    override val bookmarkId: Long? = 1
    override val contentId: Long? = 1

    override fun onBookmarkSuccess(bookmarkId: Long?) {
    }

    override fun onDeleteBookmarkSuccess() {
    }

}