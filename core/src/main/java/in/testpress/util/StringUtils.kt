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