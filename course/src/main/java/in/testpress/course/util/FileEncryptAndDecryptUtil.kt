package `in`.testpress.course.util

import androidx.annotation.VisibleForTesting
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

class FileEncryptAndDecryptUtil(val file: File) {

    private val REVERSE_BYTE_COUNT = 1024
    private val READ_WRITE = "rw"
    private var filePointer: RandomAccessFile
    private lateinit var tempFileToStoreReversedData: File

    init {
        filePointer = RandomAccessFile(file, READ_WRITE)
    }

    fun encrypt() {
        reverseFirstFewBytesOfTheFile()
        saveReversedDataInFile()
    }

    private fun reverseFirstFewBytesOfTheFile(): ByteArray {
        try {
            val data = getFirstFewBytesOfTheFile()
            reverseBytes(data)
            saveReversedDataInTempFile(data)
        } catch(e: IOException) {
            e.printStackTrace()
            return byteArrayOf()
        }
        return tempFileToStoreReversedData.readBytes()
    }

    private fun getFirstFewBytesOfTheFile(): ByteArray {
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
    fun reverseBytes(fileBytes: ByteArray): ByteArray {
        fileBytes.reverse()
        return fileBytes
    }

    private fun saveReversedDataInTempFile(reversedBytes: ByteArray) {
        tempFileToStoreReversedData = File.createTempFile("temp", "file")
        val tempRandomAccessFile = RandomAccessFile(tempFileToStoreReversedData, READ_WRITE)
        try {
            tempRandomAccessFile.write(file.readBytes())
            tempRandomAccessFile.write(reversedBytes)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        tempRandomAccessFile.close()
    }

    private fun saveReversedDataInFile() {
        filePointer.seek(0)
        filePointer.write(tempFileToStoreReversedData.readBytes())
    }

    fun decrypt(): ByteArray {
        reverseFirstFewBytesOfTheFile()
        return tempFileToStoreReversedData.readBytes()
    }

    protected fun finalize() {
        tempFileToStoreReversedData.delete()
        filePointer.close()
    }
}
