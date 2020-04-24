package `in`.testpress.util

object StringUtils{
    @JvmStatic
    fun isNullOrEmpty(str: String): Boolean {
        return str.isNullOrBlank()
    }
}