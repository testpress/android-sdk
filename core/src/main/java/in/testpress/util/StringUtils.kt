package `in`.testpress.util

import android.text.TextUtils
import android.webkit.MimeTypeMap

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
    val illegalCharactersRegex = Regex("[/`?*<>|\":\\\\]")
    return this.replace(illegalCharactersRegex, "_")
}

internal fun String?.isValidUrl():Boolean{
    return this != null && android.util.Patterns.WEB_URL.matcher(this).matches()
}

fun String.isPDF():Boolean = this.getFileType() == "pdf"

fun String.isImageFile(): Boolean = this.getFileType() == "png" || this.getFileType() == "jpg" || this.getFileType() == "gif"

fun String.getFileType(): String = MimeTypeMap.getFileExtensionFromUrl(this)