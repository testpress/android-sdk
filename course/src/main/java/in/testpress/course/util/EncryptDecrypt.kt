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

    fun encryptDownloadedFile(filePath: String) {
        try {
            val fileData = readFile(filePath)
            val secretKey = getSecretKey()
            val encodedData = encrypt(secretKey, fileData)
            saveFile(encodedData, filePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSecretKey(): SecretKey {
        val key = sharedPref.getString("secretKeyPref", null)
        if (key == null) {
            val secretKey = generateSecretKey()
            saveSecretKey(secretKey!!)
            return secretKey
        }
        val decodedKey = Base64.decode(key, Base64.NO_WRAP)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    private fun saveSecretKey(secretKey: SecretKey): String {
        val encodedKey = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
        sharedPref.edit().putString("secretKeyPref", encodedKey).apply()
        return encodedKey
    }

    private fun generateSecretKey(): SecretKey? {
        val secureRandom = SecureRandom()
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator?.init(128, secureRandom)
        return keyGenerator?.generateKey()
    }

    private fun encrypt(encryptKey: SecretKey, fileData: ByteArray): ByteArray {
        try {
            val data = encryptKey.encoded
            val secretKeySpec = SecretKeySpec(data, 0, data.size, "AES")
            val cipher = Cipher.getInstance("AES", "BC")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
            return cipher.doFinal(fileData)
        } catch(e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOf()
    }

    private fun saveFile(fileData: ByteArray, path: String) {
        val file = File(path)
        BufferedOutputStream(FileOutputStream(file, false)).apply {
            write(fileData)
            flush()
            close()
        }
    }

    fun decryptEncryptedFile(filePath: String): ByteArray {
        try {
            val fileData = readFile(filePath)
            val secretKey = getSecretKey()
            return decrypt(secretKey, fileData)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOf()
    }

    private fun readFile(filePath: String): ByteArray {
        val file = File(filePath)
        val fileContents = file.readBytes()
        val inputBuffer = BufferedInputStream(FileInputStream(file))
        inputBuffer.read(fileContents)
        inputBuffer.close()
        return fileContents
    }

    private fun decrypt(yourKey: SecretKey, fileData: ByteArray): ByteArray {
        try {
            val decrypted: ByteArray
            val cipher = Cipher.getInstance("AES", "BC")
            cipher.init(Cipher.DECRYPT_MODE, yourKey, IvParameterSpec(ByteArray(cipher.blockSize)))
            decrypted = cipher.doFinal(fileData)
            return decrypted
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return byteArrayOf()
    }
}
