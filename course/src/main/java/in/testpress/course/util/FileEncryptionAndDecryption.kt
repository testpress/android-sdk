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
            val byteCountToReverse = getBytesCountToReverse(file)

            val fileBytes = ByteArray(byteCountToReverse)
            readFile(randomAccessFile,fileBytes)
            reverseBytes(fileBytes)
            writeFile(randomAccessFile, fileBytes)
            randomAccessFile.close()

        } catch (e: IOException) {
           e.printStackTrace()
        }
    }

    private fun getBytesCountToReverse(file: File): Int {
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
        var startPosition = 0
        var endPosition = fileBytes.size - 1
        var temp: Byte
        while (endPosition > startPosition) {
            temp = fileBytes[endPosition]
            fileBytes[endPosition] = fileBytes[startPosition]
            fileBytes[startPosition] = temp
            endPosition--
            startPosition++
        }
        return fileBytes
    }

    private fun writeFile(randomAccessFile: RandomAccessFile, byteArray: ByteArray) {
        randomAccessFile.write(byteArray)
        randomAccessFile.seek(0)
    }

    fun decrypt(file: File): ByteArray {
        try {
            val byteCountToReverse = getBytesCountToReverse(file)
            val randomAccessFile = RandomAccessFile(file, READ_WRITE)
            randomAccessFile.seek(0)

            val fileBytes = ByteArray(byteCountToReverse)
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
