package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Answer(
    @PrimaryKey
    val id: Long? = null,
    val textHtml: String? = null,
    val marks: String? = null,
    val questionId: Long? = null
)