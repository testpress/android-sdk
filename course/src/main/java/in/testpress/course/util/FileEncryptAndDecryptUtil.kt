package `in`.testpress.course.util

import androidx.annotation.VisibleForTesting
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class FileEncryptAndDecryptUtil(val file: File) {

    private val REVERSE_BYTE_COUNT = 1024
    private val READ_WRITE = "rw"
    private var randomAccessFile: RandomAccessFile
    private lateinit var tempFile: File

    init {
        randomAccessFile = RandomAccessFile(file, READ_WRITE)
    }

    fun encrypt() {
        reverse()
        saveFile()
    }

    private fun reverse() {
        try {
            val reversedBytes = getBytesToReverse()
            reverseBytes(reversedBytes)
            saveReversedBytesInTempFile(reversedBytes)
        } catch (e: IOException) {
           e.printStackTrace()
        }
    }

    private fun getBytesToReverse(): ByteArray {
        val reverseByteSize = getBytesSizeToReverse(file)
        val fileBytes = ByteArray(reverseByteSize)
        readFile(fileBytes)
        return fileBytes
    }

    private fun getBytesSizeToReverse(file: File): Int {
        return if (file.length() < REVERSE_BYTE_COUNT) {
            file.length().toInt()
        } else {
            REVERSE_BYTE_COUNT
        }
    }

    private fun readFile(byteArray: ByteArray) {
        randomAccessFile.seek(0)
        randomAccessFile.read(byteArray)
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

    private fun saveFile() {
        randomAccessFile.write(tempFile.readBytes())
        randomAccessFile.seek(0)
    }

    fun decrypt(): ByteArray {
        reverse()
        return tempFile.readBytes()
    }

    protected fun finalize() {
        tempFile.delete()
        randomAccessFile.close()
    }
}
