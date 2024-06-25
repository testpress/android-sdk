package `in`.testpress.database.entities

import java.text.SimpleDateFormat
import java.util.*

data class ExamModification(
    val id: Long,
    val examDataModifiedOn: String?
) {
    fun getLastModifiedAsDate(): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        return examDataModifiedOn?.let { dateFormat.parse(it) }
    }
}