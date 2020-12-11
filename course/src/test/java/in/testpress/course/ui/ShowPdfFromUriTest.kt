package `in`.testpress.course.ui

import `in`.testpress.course.fragments.PdfUtil
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
            PdfUtil(pdfViewerActivity).get("http://www.pdf995.com/samples/pdf.pdf")
        }
    }

    @Test
    fun forIncorrectUrlInputStreamShouldBeNull() {
        PdfUtil(pdfViewerActivity).get("")
        Assert.assertNull( PdfUtil(pdfViewerActivity).inputStream)
    }
}
