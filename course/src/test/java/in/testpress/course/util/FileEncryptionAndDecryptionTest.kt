package `in`.testpress.course.util

import org.junit.Assert
import org.junit.Test

class FileEncryptionAndDecryptionTest {

    private lateinit var bytes: ByteArray

    @Test
    fun testReverseBytesReversesBytesCorrectly() {
        val inputString: String = "testPress"
        bytes = inputString.byteInputStream().readBytes()

        val result = FileEncryptionAndDecryption.reverseBytes(bytes)
        Assert.assertEquals("sserPtset",  String(result))
    }
}
