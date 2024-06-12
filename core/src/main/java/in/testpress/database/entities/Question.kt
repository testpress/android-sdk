package `in`.testpress.database.entities

import androidx.room.Embedded
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
    @Embedded
    val direction: Direction? = null,
    @Embedded
    val answers: List<Answer>? = null,
    @Embedded
    val translations: ArrayList<Question> = ArrayList(),
)