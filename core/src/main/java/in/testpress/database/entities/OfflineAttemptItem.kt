package `in`.testpress.database.entities

import androidx.room.Entity

@Entity
data class OfflineAttemptItem(
    val id: Int? = null,
    val url: String? = null,
    val question: OfflineAttemptQuestion? = null,
    val selectedAnswers: List<Int> = ArrayList(),
    val review: Boolean? = null,
    var savedAnswers: List<Int> = ArrayList(),
    val index: Int? = null,
    val currentReview: Boolean? = null,
    val shortText: String? = null,
    val currentShortText: String? = null,
    val attemptSection: OfflineAttemptSection? = null,
    val essayText: String? = null,
    val localEssayText: String? = null,
    val files: List<OfflineUserUploadedFile> = ArrayList(),
    val unSyncedFiles: List<String> = ArrayList()
)










