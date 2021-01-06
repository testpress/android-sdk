package `in`.testpress.course.util

import androidx.annotation.VisibleForTesting
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class FileEncryptionAndDecryption {

    private val REVERSE_BYTE_COUNT = 1024

    private val READ_WRITE = "rw"

    fun encrypt(file: File) {
        try {
            val randomAccessFile = RandomAccessFile(file, READ_WRITE)
            randomAccessFile.seek(0)
            val reverseByteSize = getBytesSizeToReverse(file)

            val fileBytes = ByteArray(reverseByteSize)
            readFile(randomAccessFile,fileBytes)
            reverseBytes(fileBytes)
            writeFile(randomAccessFile, fileBytes)
            randomAccessFile.close()

        } catch (e: IOException) {
           e.printStackTrace()
        }
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
        randomAccessFile.seek(0)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun reverseBytes(fileBytes: ByteArray?): ByteArray {
        if (fileBytes == null) return byteArrayOf()
        fileBytes.reverse()
        return fileBytes
    }

    private fun writeFile(randomAccessFile: RandomAccessFile, byteArray: ByteArray) {
        randomAccessFile.write(byteArray)
        randomAccessFile.seek(0)
    }

    fun decrypt(file: File): ByteArray {
        try {
            val reverseByteSize = getBytesSizeToReverse(file)
            val randomAccessFile = RandomAccessFile(file, READ_WRITE)
            randomAccessFile.seek(0)

            val fileBytes = ByteArray(reverseByteSize)
            readFile(randomAccessFile,fileBytes)
            reverseBytes(fileBytes)
            writeFile(randomAccessFile, fileBytes)
            randomAccessFile.close()

            return file.readBytes()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOf()
    }
}
