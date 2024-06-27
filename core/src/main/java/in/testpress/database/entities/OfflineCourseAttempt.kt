package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineCourseAttempt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 1,
    val assessmentId: Long
)