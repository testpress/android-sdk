package `in`.testpress.course.util

import java.security.MessageDigest

object SHA256Generator {
    fun String.generateSha256(): String {
        val digest = MessageDigest.getInstance("SHA-256").apply { reset() }
        val byteData: ByteArray = digest.digest(this.toByteArray())
        return StringBuffer().apply {
            byteData.forEach {
                append(((it.toInt() and 0xff) + 0x100).toString(16).substring(1))
            }
        }.toString()
    }
}
