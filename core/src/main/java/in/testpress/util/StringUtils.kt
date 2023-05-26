package `in`.testpress.util

import android.text.TextUtils

object StringUtils{
    @JvmStatic
    fun isNullOrEmpty(str: String?): Boolean {
        return str.isNullOrBlank()
    }
}

fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Sanitizes the file name by replacing illegal characters with an underscore.
 *
 * @return The sanitized file name.
 */
fun String.sanitizeFileName(): String {
    val illegalCharactersRegex = Regex("[<>:|/?*]")
    return this.replace(illegalCharactersRegex, "_")
}