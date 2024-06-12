package `in`.testpress.database.entities

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class ExamQuestion(
    val id: Long? = null,
    val order: Int? = null,
    val examId: Long? = null,
    val attemptId: Long? = null,
    val questionId: Long? = null,
    @Embedded
    val question: Question? = null
)




