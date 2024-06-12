package `in`.testpress.database.entities

import androidx.room.Entity

@Entity
data class OfflineAttemptQuestion(
    val questionHtml: String? = null,
    val answers: List<OfflineAttemptAnswer> = ArrayList(),
    val subject: String? = null,
    val direction: String? = null,
    val type: String? = null,
    val language: String? = null,
    val translations: ArrayList<OfflineAttemptQuestion> = ArrayList(),
    val marks: String? = null,
    val negativeMarks: String? = null
)
