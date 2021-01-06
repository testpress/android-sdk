package `in`.testpress.course.util

import androidx.annotation.VisibleForTesting
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class FileReversal {

    private val REVERSE_BYTE_COUNT = 1024

    private val READ_WRITE = "rw"

    private lateinit var file: File

    fun reverse(file: File): ByteArray {
        try {
            this.file = file

            val randomAccessFile = RandomAccessFile(file, READ_WRITE)
            goToStartOfTheFile(randomAccessFile)
            val fileBytes = getBytesToReverse(randomAccessFile)
            reverseBytes(fileBytes)
            writeFile(randomAccessFile, fileBytes)
            randomAccessFile.close()

            return file.readBytes()

        } catch (e: IOException) {
           e.printStackTrace()
        }
        return byteArrayOf()
    }

    private fun goToStartOfTheFile(randomAccessFile: RandomAccessFile) {
        randomAccessFile.seek(0)
    }

    private fun getBytesToReverse(randomAccessFile: RandomAccessFile): ByteArray {
        val reverseByteSize = getBytesSizeToReverse(file)
        val fileBytes = ByteArray(reverseByteSize)
        readFile(randomAccessFile,fileBytes)
        return fileBytes
    }

    private fun getBytesSizeToReverse(file: File): Int {
        return if (file.length() < REVERSE_BYTE_COUNT) {
            file.length().toInt()
        } else {
            REVERSE_BYTE_COUNT
        }
    }

    private fun readFile(randomAccessFile: RandomAccessFile,byteArray: ByteArray) {
        randomAccessFile.read(byteArray)
        goToStartOfTheFile(randomAccessFile)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun reverseBytes(fileBytes: ByteArray?): ByteArray {
        if (fileBytes == null) return byteArrayOf()
        fileBytes.reverse()
        return fileBytes
    }

    private fun writeFile(randomAccessFile: RandomAccessFile, byteArray: ByteArray) {
        randomAccessFile.write(byteArray)
        goToStartOfTheFile(randomAccessFile)
    }
}
