// package `in`.testpress.course.fragments
//
// import `in`.testpress.core.TestpressSDKDatabase
// import `in`.testpress.core.TestpressSdk
// import `in`.testpress.core.TestpressSession
// import `in`.testpress.course.ui.ContentActivity
// import `in`.testpress.course.util.GreendaoCleanupMixin
// import `in`.testpress.course.viewmodels.ContentViewModel
// import `in`.testpress.models.InstituteSettings
// import `in`.testpress.models.greendao.Attachment
// import `in`.testpress.models.greendao.Chapter
// import `in`.testpress.models.greendao.Content
// import android.os.Build
// import android.os.Bundle
// import androidx.arch.core.executor.testing.InstantTaskExecutorRule
// import androidx.test.core.app.ApplicationProvider
// import org.junit.Before
// import org.junit.Rule
// import org.junit.Test
// import org.junit.runner.RunWith
// import org.mockito.Mockito
// import org.mockito.Mockito.doNothing
// import org.mockito.Mockito.verify
// import org.mockito.MockitoAnnotations
// import org.robolectric.RobolectricTestRunner
// import org.robolectric.annotation.Config
// import org.robolectric.shadows.support.v4.SupportFragmentController
//
// @Config(sdk = [Build.VERSION_CODES.O_MR1])
// @RunWith(RobolectricTestRunner::class)
// class AttachmentContentFragmentTest : GreendaoCleanupMixin() {
//     @Rule
//     @JvmField
//     val instantExecutorRule = InstantTaskExecutorRule()
//     var content = Content()
//     private val chapter = Chapter()
//     private val contentDao =
//         TestpressSDKDatabase.getContentDao(ApplicationProvider.getApplicationContext())
//     lateinit var contentFragment: AttachmentContentFragment
//
//     @Before
//     fun setUp() {
//         MockitoAnnotations.initMocks(this)
//         val instituteSettings = InstituteSettings("http://localhost:9200")
//         TestpressSdk.setTestpressSession(
//             ApplicationProvider.getApplicationContext(),
//             TestpressSession(instituteSettings, "USER_TOKEN")
//         )
//     }
//
//     fun setUpChapterAndContent() {
//         chapter.id = 1
//         chapter.name = "Chapter"
//         content.title = "New Content"
//         content.attachment = Attachment(1)
//         content.id = 1
//         content.active = true
//         content.chapter = chapter
//         content.isLocked = false
//         contentDao.insertOrReplaceInTx(content)
//     }
//
//     private fun initializeContentFragment() {
//         val bundle = Bundle()
//         bundle.putLong(ContentActivity.CHAPTER_ID, 1)
//         bundle.putInt(ContentActivity.POSITION, 0)
//
//         contentFragment = AttachmentContentFragment()
//         contentFragment = Mockito.spy(contentFragment)
//         contentFragment.arguments = bundle
//         doNothing().`when`(contentFragment).forceReloadContent()
//         SupportFragmentController.setupFragment(contentFragment)
//
//         contentFragment.viewModel = Mockito.mock(ContentViewModel::class.java)
//     }
//
//     @Test
//     fun testDisplayCreatesContentAttempt() {
//         setUpChapterAndContent()
//         initializeContentFragment()
//         contentFragment.display()
//
//         verify(contentFragment.viewModel).createContentAttempt(1)
//     }
// }