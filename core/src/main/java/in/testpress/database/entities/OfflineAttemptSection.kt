package `in`.testpress.database.entities

import androidx.room.Entity

@Entity
data class OfflineAttemptSection(
    val attemptSectionId: Long? = null,
    val state: String? = null,
    val questionsUrl: String? = null,
    val startUrl: String? = null,
    val endUrl: String? = null,
    var remainingTime: String? = null,
    val name: String? = null,
    val duration: String? = null,
    val order: Int? = null,
    val instructions: String? = null,
    val attemptId: Long? = null,
)
