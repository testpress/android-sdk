package `in`.testpress.course.ui

import `in`.testpress.course.util.PdfDownloadUtil
import android.content.Context
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PdfDownloadUtilTest {

    @Mock
    lateinit var pdfViewerActivity: PdfViewerActivity

    lateinit var context: Context

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        context = RuntimeEnvironment.application
    }

    @Test
    fun correctUrlShouldNotThrowException() {
        assertDoesNotThrow {
            PdfDownloadUtil(pdfViewerActivity).downloadPdfFromInternet(
                    "http://www.pdf995.com/samples/pdf.pdf",
                    fileName = "fileName",
                    context = context
            )
        }
    }

    @Test
    fun forIncorrectUrlInputStreamShouldBeNull() {
        PdfDownloadUtil(pdfViewerActivity).downloadPdfFromInternet(
                "",
                fileName = "fileName",
                context = context
        )
        Assert.assertNull( PdfDownloadUtil(pdfViewerActivity).file)
    }
}
