package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Question(
    @PrimaryKey
    val id: Long? = null,
    var questionHtml: String? = null,
    val directionId: Long? = null,
    val answers: ArrayList<Answer> = arrayListOf(),
    val language: String? = null,
    val subjectId: Long? = null,
    val type: String? = null,
    val translations: ArrayList<Question> = arrayListOf(),
    val marks: String? = null,
    val negativeMarks: String? = null,
    val parentId: Long? = null,
)