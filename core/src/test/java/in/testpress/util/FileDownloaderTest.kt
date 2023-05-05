package `in`.testpress.util

import android.app.DownloadManager
import android.content.Context
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FileDownloaderTest {

    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var downloadManager: DownloadManager
    private lateinit var fileDownloader: FileDownloader
    @Captor
    private lateinit var captor: ArgumentCaptor<DownloadManager.Request>

    @Before
    fun setUp() {
        fileDownloader = FileDownloader(context)
        `when`(context.getSystemService(Context.DOWNLOAD_SERVICE)).thenReturn(downloadManager)
    }

    @Test
    fun `test downloadFile`() {
        val fileUrl = "https://example.com/file.pdf"
        val fileName = "file.pdf"
        fileDownloader.downloadFile(fileUrl, fileName)
        verify(downloadManager).enqueue(captor.capture())
    }

}