package `in`.testpress.database.entities

import androidx.room.Entity

@Entity
data class Answer(
    val id: Long? = null,
    val textHtml: String? = null,
    val marks: String? = null,
    val questionId: Long? = null
)