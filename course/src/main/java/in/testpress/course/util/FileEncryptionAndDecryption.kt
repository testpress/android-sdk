package `in`.testpress.course.util

import androidx.annotation.VisibleForTesting
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

object FileEncryptionAndDecryption {

    const val REVERSE_BYTE_COUNT = 1024

    fun decrypt(file: File): ByteArray {
        try {
            val byteToReverse = getBytesCountToReverse(file)
            val randomAccessFile = RandomAccessFile(file, "rw")
            randomAccessFile.seek(0)

            var byteArray = ByteArray(byteToReverse)
            readFile(randomAccessFile,byteArray)
            reverseBytes(byteArray)
            writeFile(randomAccessFile, byteArray)

            byteArray = ByteArray(byteToReverse)
            randomAccessFile.read(byteArray)
            randomAccessFile.close()

            return file.readBytes()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOf()
    }

    private fun readFile(randomAccessFile: RandomAccessFile,byteArray: ByteArray) {
        randomAccessFile.read(byteArray)
        randomAccessFile.seek(0)
    }

    private fun writeFile(randomAccessFile: RandomAccessFile, byteArray: ByteArray) {
        randomAccessFile.write(byteArray)
        randomAccessFile.seek(0)
    }

    fun encrypt(file: File) {
        try {
            val randomAccessFile = RandomAccessFile(file, "rw")
            randomAccessFile.seek(0)
            val byteToReverse = getBytesCountToReverse(file)

            var byteArray = ByteArray(byteToReverse)
            readFile(randomAccessFile,byteArray)
            reverseBytes(byteArray)
            writeFile(randomAccessFile, byteArray)

            byteArray = ByteArray(byteToReverse)
            randomAccessFile.read(byteArray)
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun reverseBytes(array: ByteArray?): ByteArray {
        if (array == null) return byteArrayOf()
        var startPosition = 0
        var endPosition = array.size - 1
        var temp: Byte
        while (endPosition > startPosition) {
            temp = array[endPosition]
            array[endPosition] = array[startPosition]
            array[startPosition] = temp
            endPosition--
            startPosition++
        }
        return array
    }
}
