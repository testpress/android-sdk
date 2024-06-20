package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExamQuestion(
    @PrimaryKey
    val id: Long? = null,
    val order: Int? = null,
    val examId: Long? = null,
    val attemptId: Long? = null,
    val questionId: Long? = null,
)