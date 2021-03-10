// package `in`.testpress.course.fragments
//
// import android.content.Context
// import androidx.arch.core.executor.testing.InstantTaskExecutorRule
// import androidx.test.core.app.ApplicationProvider
// import org.junit.Before
// import org.junit.Rule
// import org.junit.Test
// import org.junit.jupiter.api.Assertions.assertDoesNotThrow
// import org.junit.runner.RunWith
// import org.mockito.Mock
// import org.mockito.MockitoAnnotations
// import org.robolectric.RobolectricTestRunner
//
// @RunWith(RobolectricTestRunner::class)
// class DocumentViewerFragmentTest {
//
//     @Rule
//     @JvmField
//     val instantExecutorRule = InstantTaskExecutorRule()
//
//     @Mock
//     lateinit var documentViewerFragment: DocumentViewerFragment
//
//     private var context: Context = ApplicationProvider.getApplicationContext()
//
//
//     @Before
//     fun setup() {
//         MockitoAnnotations.initMocks(this)
//         val pdfDownloader = PDFDownloader(documentViewerFragment, context, "")
//         documentViewerFragment.pdfDownloader = pdfDownloader
//     }
//
//     @Test
//     fun testAppShouldNotCrashWhenMenuIsNotInitialized() {
//         assertDoesNotThrow {
//             documentViewerFragment.onPDFLoaded()
//         }
//     }
//
//     @Test
//     fun testAppShouldNotCrashWhenClosingTheFragment() {
//         assertDoesNotThrow {
//             documentViewerFragment.onDetach()
//         }
//     }
// }
// package `in`.testpress.course.fragments
//
// import android.content.Context
// import androidx.arch.core.executor.testing.InstantTaskExecutorRule
// import androidx.test.core.app.ApplicationProvider
// import org.junit.Before
// import org.junit.Rule
// import org.junit.Test
// import org.junit.jupiter.api.Assertions.assertDoesNotThrow
// import org.junit.runner.RunWith
// import org.mockito.Mock
// import org.mockito.MockitoAnnotations
// import org.robolectric.RobolectricTestRunner
//
// @RunWith(RobolectricTestRunner::class)
// class DocumentViewerFragmentTest {
//
//     @Rule
//     @JvmField
//     val instantExecutorRule = InstantTaskExecutorRule()
//
//     @Mock
//     lateinit var documentViewerFragment: DocumentViewerFragment
//
//     private var context: Context = ApplicationProvider.getApplicationContext()
//
//
//     @Before
//     fun setup() {
//         MockitoAnnotations.initMocks(this)
//         val pdfDownloader = PDFDownloader(documentViewerFragment, context, "")
//         documentViewerFragment.pdfDownloader = pdfDownloader
//     }
//
//     @Test
//     fun testAppShouldNotCrashWhenMenuIsNotInitialized() {
//         assertDoesNotThrow {
//             documentViewerFragment.onPDFLoaded()
//         }
//     }
//
//     @Test
//     fun testAppShouldNotCrashWhenClosingTheFragment() {
//         assertDoesNotThrow {
//             documentViewerFragment.onDetach()
//         }
//     }
// }
