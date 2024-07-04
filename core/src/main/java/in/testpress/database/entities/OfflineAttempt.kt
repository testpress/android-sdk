package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineAttempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val totalQuestions: Int,
    val lastStartedTime: String,
    val remainingTime: String,
    val timeTaken: String,
    val state: String,
    val attemptType: Int,
    val examId: Long
)
