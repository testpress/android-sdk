package `in`.testpress.database.entities

import java.text.SimpleDateFormat
import java.util.*

data class ExamModification(
    val contentId: Long,
    val lastModified: String
) {
    fun getLastModifiedAsDate(): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        return dateFormat.parse(lastModified)
    }
}