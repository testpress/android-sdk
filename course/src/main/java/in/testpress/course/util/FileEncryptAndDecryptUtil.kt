package `in`.testpress.course.util

import androidx.annotation.VisibleForTesting
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class FileEncryptAndDecryptUtil(val file: File) {

    private val REVERSE_BYTE_COUNT = 1024
    private val READ_WRITE = "rw"
    private var filePointer: RandomAccessFile
    private lateinit var tempFile: File

    init {
        filePointer = RandomAccessFile(file, READ_WRITE)
    }

    fun encrypt() {
        saveFile(reverse())
    }

    private fun reverse(): ByteArray {
        try {
            val bytesToReverse = getBytesToReverse()
            reverseBytes(bytesToReverse)
            saveReversedBytesInTempFile(bytesToReverse)
        } catch(e: IOException) {
            e.printStackTrace()
            return byteArrayOf()
        }
        return tempFile.readBytes()
    }

    private fun getBytesToReverse(): ByteArray {
        val reverseByteSize = if (file.length() < REVERSE_BYTE_COUNT) {
            file.length().toInt()
        } else {
            REVERSE_BYTE_COUNT
        }
        val fileBytes = ByteArray(reverseByteSize)
        filePointer.seek(0)
        filePointer.read(fileBytes)
        return fileBytes
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun reverseBytes(fileBytes: ByteArray?): ByteArray {
        if (fileBytes == null) return byteArrayOf()
        fileBytes.reverse()
        return fileBytes
    }

    private fun saveReversedBytesInTempFile(reversedBytes: ByteArray) {
        tempFile = File.createTempFile("temp", "file")
        val tempRandomAccessFile = RandomAccessFile(tempFile, READ_WRITE)
        try {
            tempRandomAccessFile.write(file.readBytes())
            tempRandomAccessFile.write(reversedBytes)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        tempRandomAccessFile.close()
    }

    private fun saveFile(fileBytes: ByteArray) {
        filePointer.write(fileBytes)
        filePointer.seek(0)
    }

    fun decrypt(): ByteArray {
        reverse()
        return tempFile.readBytes()
    }

    protected fun finalize() {
        tempFile.delete()
        filePointer.close()
    }
}
