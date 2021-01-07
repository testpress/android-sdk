package `in`.testpress.course.util

import org.junit.Assert
import org.junit.Test
import java.io.File

class FileReversalTest {

    private lateinit var bytes: ByteArray

    private var fileReversal = FileReversal()

    @Test
    fun testReverseBytesReversesBytesCorrectly() {
        val inputString = "testPress"
        bytes = inputString.byteInputStream().readBytes()

        val result = FileReversal().reverseBytes(bytes)
        Assert.assertEquals("sserPtset", String(result))
    }

    @Test
    fun decryptedFileAndOriginalFileShouldBeSame() {
        val resource = ClassLoader.getSystemResource("dummy.pdf")
        val originalFile = File(resource.file)

        val reversedBytes = fileReversal.reverse(originalFile)
        fileReversal.saveFile(reversedBytes)
        val encryptedFile = File(resource.file)
        val decryptedFile = fileReversal.reverse(encryptedFile)

        Assert.assertEquals(String(decryptedFile), String(originalFile.readBytes()))
    }
}
