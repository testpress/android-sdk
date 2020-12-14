package `in`.testpress.course.ui

import `in`.testpress.course.util.PDFDownloader
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
class PDFDownloaderTest {

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
            PDFDownloader(pdfViewerActivity).download(
                    "http://www.pdf995.com/samples/pdf.pdf",
                    fileName = "fileName",
                    context = context
            )
        }
    }

    @Test
    fun forIncorrectUrlInputStreamShouldBeNull() {
        PDFDownloader(pdfViewerActivity).download(
                "",
                fileName = "fileName",
                context = context
        )
        Assert.assertNull(PDFDownloader(pdfViewerActivity).file)
    }
}
