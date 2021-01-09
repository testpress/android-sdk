package `in`.testpress.course.util

import androidx.annotation.VisibleForTesting
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

class FileReversal {

    private val REVERSE_BYTE_COUNT = 1024

    private val READ_WRITE = "rw"

    private lateinit var randomAccessFile: RandomAccessFile

    private lateinit var file: File

    private lateinit var tempFile: File


    fun reverse(file: File): ByteArray {
        try {
            this.file = file
            randomAccessFile = RandomAccessFile(file, READ_WRITE)
            goToStartOfTheFile()
            val reversedBytes = getBytesToReverse()
            reverseBytes(reversedBytes)
            saveReversedBytesInTempFile(reversedBytes)
            return tempFile.readBytes()

        } catch (e: IOException) {
           e.printStackTrace()
        }
        return byteArrayOf()
    }

    private fun goToStartOfTheFile() {
        randomAccessFile.seek(0)
    }

    private fun getBytesToReverse(): ByteArray {
        val reverseByteSize = getBytesSizeToReverse(file)
        val fileBytes = ByteArray(reverseByteSize)
        readFile(randomAccessFile, fileBytes)
        return fileBytes
    }

    private fun getBytesSizeToReverse(file: File): Int {
        return if (file.length() < REVERSE_BYTE_COUNT) {
            file.length().toInt()
        } else {
            REVERSE_BYTE_COUNT
        }
    }

    private fun readFile(randomAccessFile: RandomAccessFile, byteArray: ByteArray) {
        randomAccessFile.read(byteArray)
        goToStartOfTheFile()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun reverseBytes(fileBytes: ByteArray?): ByteArray {
        if (fileBytes == null) return byteArrayOf()
        fileBytes.reverse()
        return fileBytes
    }

    private fun saveReversedBytesInTempFile(reversedBytes: ByteArray) {
        tempFile = File.createTempFile("temp", "file")
        val stream = FileOutputStream(tempFile)
        try {
            stream.write(file.readBytes())
            stream.write(reversedBytes)
        } finally {
            stream.close()
        }
    }

    fun saveFile(fileBytes: ByteArray?) {
        randomAccessFile.write(fileBytes)
        goToStartOfTheFile()
        randomAccessFile.close()
    }

    fun deleteTempFile() {
        tempFile.delete()
    }
}
