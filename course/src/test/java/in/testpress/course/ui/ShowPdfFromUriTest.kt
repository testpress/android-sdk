package `in`.testpress.course.ui

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ShowPdfFromUriTest {

    @Mock
    lateinit var activity: PdfViewerActivity

    private lateinit var showPdfFromUri: ShowPdfFromUri

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        showPdfFromUri = ShowPdfFromUri(activity)
    }

    @Test
    fun forCorrectUrlPdfShouldDisplayWithoutException() {
        assertDoesNotThrow {
            showPdfFromUri.execute("http://www.pdf995.com/samples/pdf.pdf")
        }
    }

    @Test
    fun whenUrlIsEmptyPdfShouldNotDisplay() {
        showPdfFromUri.execute("")
        Assert.assertNull(showPdfFromUri.inputStream)
    }
}
