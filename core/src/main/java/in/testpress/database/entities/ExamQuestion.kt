package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExamQuestion(
    @PrimaryKey
    val id: Long? = null,
    val order: Int? = null,
    val questionId: Long? = null,
    val sectionId: Long? = null,
    val marks: String? = null,
    val partialMarks: String? = null,
    val examId: Long? = null,
)