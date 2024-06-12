package `in`.testpress.database.entities

import androidx.room.Entity

@Entity
data class Question(
    val id: Long? = null,
    val questionHtml: String? = null,
    val parentId: Long? = null,
    val type: String? = null,
    val subjectId: Long? = null,
    val answerIds: List<Int>? = null,
    val directionId: Long? = null,
    val direction: Direction? = null,
    val answers: List<Answer>? = null,
    val translations: ArrayList<Question> = ArrayList(),
)






