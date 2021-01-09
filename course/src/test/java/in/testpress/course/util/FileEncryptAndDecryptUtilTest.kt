package `in`.testpress.course.util

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class FileEncryptAndDecryptUtilTest {

    private lateinit var bytes: ByteArray
    private lateinit var fileEncryptAndDecryptUtil: FileEncryptAndDecryptUtil
    private lateinit var file: File

    @Before
    fun setup() {
        val resource = ClassLoader.getSystemResource("dummy.pdf")
        file = File(resource.file)
        fileEncryptAndDecryptUtil = FileEncryptAndDecryptUtil(file)
    }

    @Test
    fun testReverseBytesReversesBytesCorrectly() {
        val inputString = "testPress"
        bytes = inputString.byteInputStream().readBytes()

        val result = fileEncryptAndDecryptUtil.reverseBytes(bytes)
        Assert.assertEquals("sserPtset", String(result))
    }

    @Test
    fun decryptedFileAndOriginalFileShouldBeSame() {
        val originalFile = file
        fileEncryptAndDecryptUtil.encrypt()
        val decryptedFile = fileEncryptAndDecryptUtil.decrypt()

        Assert.assertEquals(String(decryptedFile), String(originalFile.readBytes()))
    }
}
