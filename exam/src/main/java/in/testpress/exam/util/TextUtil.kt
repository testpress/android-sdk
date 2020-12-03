package `in`.testpress.exam.util

import androidx.core.text.HtmlCompat

object TextUtil {
    @JvmStatic
    fun removeHtmlTags(html: String): String {
        val text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
        return text.replace("@font-face \\{.*\\}".toRegex(), "").trim()
    }
}
