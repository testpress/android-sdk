package `in`.testpress.course.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FileEncryptionAndDecryptionTest {

    private lateinit var bytes: ByteArray

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testReverseBytesReversesBytesCorrectly() {
        val inputString = "testPress"
        bytes = inputString.byteInputStream().readBytes()

        val result = FileEncryptionAndDecryption().reverseBytes(bytes)
        Assert.assertEquals("sserPtset", String(result))
    }

    @Test
    fun decryptedFileAndOriginalFileShouldBeSame() {
        val resource = ClassLoader.getSystemResource("dummy.pdf")
        val originalFile = File(resource.file)

        FileEncryptionAndDecryption().encrypt(originalFile)
        val encryptedFile = File(resource.file)
        val decryptedFile = FileEncryptionAndDecryption().decrypt(encryptedFile)

        Assert.assertEquals(String(decryptedFile), String(originalFile.readBytes()))
    }
}
