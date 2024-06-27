package `in`.testpress.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OfflineAttemptSection(
    var id: Long,
    @PrimaryKey(autoGenerate = true)
    val attemptSectionId: Long = 1,
    val state: String,
    val remainingTime: String? = null,
    val name: String,
    val duration: String,
    val order: Int,
    val instructions: String? = null,
    val attemptId: Long,
)
