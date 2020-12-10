package `in`.testpress.course.ui

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ShowPdfFromUriTest {

    @Mock
    lateinit var pdfViewerActivity: PdfViewerActivity

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun correctUrlShouldNotThrowException() {
        assertDoesNotThrow {
            pdfViewerActivity.ShowPdfFromUri().execute("http://www.pdf995.com/samples/pdf.pdf")
        }
    }

    @Test
    fun forIncorrectUrlInputStreamShouldBeNull() {
        pdfViewerActivity.ShowPdfFromUri().execute()
        Assert.assertNull(pdfViewerActivity.ShowPdfFromUri().inputStream)
    }
}
