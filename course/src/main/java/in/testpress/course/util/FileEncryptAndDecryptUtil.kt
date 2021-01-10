package `in`.testpress.course.util

import android.os.AsyncTask
import android.os.Handler
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

    private fun reverseFirstFewBytesOfTheFile() {
        try {
            val data = getFirstFewBytesOfTheFile()
            reverseBytes(data)
            saveReversedDataInTempFile(data)
        } catch(e: IOException) {
            e.printStackTrace()
        }
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
            val chunk = ByteArray(4096)
            val inputStream = file.inputStream()
            var read = inputStream.read(chunk)
            while (read != -1) {
                tempRandomAccessFile.write(chunk, 0, read)
                read = inputStream.read(chunk)
            }
            tempRandomAccessFile.write(reversedBytes)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        tempRandomAccessFile.close()
    }

    private fun saveReversedDataInFile() {
        filePointer.seek(0)
        val inputStream = tempFileToStoreReversedData.inputStream()
        val chunk = ByteArray(4096)
        var read = inputStream.read(chunk)
        while (read != -1) {
            filePointer.write(chunk, 0, read)
            read = inputStream.read(chunk)
        }
    }

    fun decrypt(): File {
        reverseFirstFewBytesOfTheFile()
        return tempFileToStoreReversedData
    }

    protected fun finalize() {
        filePointer.close()
    }

    fun cleanup() {
        AsyncTask.execute {
            tempFileToStoreReversedData.delete()
        }
    }
}
