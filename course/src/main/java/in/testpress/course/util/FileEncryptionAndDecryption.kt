package `in`.testpress.course.util

import android.content.Context
import android.util.Base64
import java.io.*
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class FileEncryptionAndDecryption(val context: Context) {

    private var sharedPref = context.applicationContext.getSharedPreferences("secretKeyPref", 0)

    fun encrypt(file: File) {
        try {
            val encodedData = getEncodedData(getKey(), read(file))
            saveFile(encodedData, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getKey(): SecretKey {
        val key = sharedPref.getString("secretKeyPref", null)
        if (key == null) {
            val generatedKey = generateKey()
            saveKey(Base64.encodeToString(generateKey()?.encoded, Base64.NO_WRAP))
            return generatedKey!!
        }
        val decodedKey = Base64.decode(key, Base64.NO_WRAP)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    private fun generateKey(): SecretKey? {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator?.init(128, SecureRandom())
        return keyGenerator?.generateKey()
    }

    private fun saveKey(key: String) {
        sharedPref.edit().putString("secretKeyPref", key).apply()
    }

    private fun read(file: File): ByteArray {
        BufferedInputStream(FileInputStream(file)).apply {
            read(file.readBytes())
            close()
        }
        return file.readBytes()
    }

    private fun getEncodedData(key: SecretKey, data: ByteArray): ByteArray {
        try {
            val secretKeySpec = SecretKeySpec(key.encoded, 0, key.encoded.size, "AES")
            Cipher.getInstance("AES", "BC").apply {
                init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(ByteArray(this.blockSize)))
                return doFinal(data)
            }
        } catch(e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOf()
    }

    private fun saveFile(data: ByteArray, file: File) {
        BufferedOutputStream(FileOutputStream(file, false)).apply {
            write(data)
            flush()
            close()
        }
    }

    fun decrypt(file: File): ByteArray {
        try {
            Cipher.getInstance("AES", "BC").apply {
                init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(ByteArray(this.blockSize)))
                return doFinal(read(file.absoluteFile))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOf()
    }
}
