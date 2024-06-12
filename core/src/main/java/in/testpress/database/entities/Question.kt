package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Question(
    @PrimaryKey
    val id: Long? = null,
    val questionHtml: String? = null,
    val parentId: Long? = null,
    val type: String? = null,
    val subjectId: Long? = null,
    val answerIds: List<Int>? = null,
    val directionId: Long? = null,
    val translations: ArrayList<Question> = arrayListOf(),
)






