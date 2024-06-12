package `in`.testpress.database.entities

import androidx.room.Entity

@Entity
data class OfflineAttemptAnswer(
    var textHtml: String? = null,
    var id: Int? = null,
)
